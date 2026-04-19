package com.analyticskit

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.analyticskit.internal.AnalyticsException
import com.analyticskit.internal.EventDispatcher
import com.analyticskit.internal.EventQueue
import com.analyticskit.internal.EventStore
import com.analyticskit.internal.IEventDispatcher
import com.analyticskit.internal.IEventStore
import com.analyticskit.internal.InMemoryEventStore
import com.analyticskit.internal.RetryPolicy
import com.analyticskit.internal.toInternal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * AnalyticsKit — A modern, lightweight analytics SDK for Android.
 *
 * Initialize once, then track events throughout your application.
 *
 * ```kotlin
 * // Initialize (typically in Application.onCreate)
 * val analytics = AnalyticsKit.initialize(
 *     context = applicationContext,
 *     config = AnalyticsConfig(apiKey = "your_api_key")
 * )
 *
 * // Track events
 * analytics.track("button_clicked", mapOf("button" to "checkout"))
 *
 * // Observe state
 * analytics.state.collect { state -> ... }
 * ```
 */
public class AnalyticsKit private constructor(
    private val config: AnalyticsConfig,
    private val store: IEventStore,
    private val eventDispatcher: IEventDispatcher,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val scope = CoroutineScope(
        SupervisorJob() + coroutineDispatcher + CoroutineName("AnalyticsKit")
    )

    private val retryPolicy = RetryPolicy()

    // LazyThreadSafetyMode.NONE — all access to queue is confined to the SDK's
    // coroutine scope (single-threaded dispatch), so the default synchronized
    // lazy is unnecessary overhead here.
    private val queue: EventQueue by lazy(LazyThreadSafetyMode.NONE) {
        EventQueue(
            config = config.batching,
            store = store,
            dispatcher = eventDispatcher,
            logLevel = config.logging
        )
    }

    @Volatile private var destroyed = false

    private val _state = MutableStateFlow(
        AnalyticsState(queuedEvents = 0, deliveryStatus = DeliveryStatus.Idle)
    )

    /** The current state of the analytics pipeline. */
    public val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        startAutoFlush()
    }

    /**
     * Tracks an event with a name and optional properties.
     *
     * @param name The event name.
     * @param properties Optional key-value pairs.
     * @throws IllegalStateException if [destroy] has been called.
     */
    public fun track(name: String, properties: Map<String, Any> = emptyMap()) {
        track(Event(name = name, properties = properties))
    }

    /**
     * Tracks a structured [Event].
     *
     * @param event The event to track.
     * @throws IllegalStateException if [destroy] has been called.
     */
    public fun track(event: Event) {
        check(!destroyed) {
            "AnalyticsKit has been destroyed. Call initialize() to create a new instance."
        }
        scope.launch {
            try {
                val events = config.eventInterceptor?.intercept(listOf(event))
                    ?: listOf(event)
                events.forEach { queue.enqueue(it.toInternal()) }
                _state.update { it.copy(queuedEvents = queue.size) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(LogLevel.ERROR, "Failed to track event '${event.name}': ${e.message}")
            }
        }
    }

    /**
     * Forces immediate delivery of all queued events.
     */
    public fun flush() {
        scope.launch {
            performFlush()
        }
    }

    /**
     * Suspending version of [flush] — waits until delivery completes.
     */
    public suspend fun flushAsync() {
        performFlush()
    }

    private suspend fun performFlush() {
        val currentSize = queue.size
        if (currentSize == 0) return

        _state.update { it.copy(deliveryStatus = DeliveryStatus.Flushing(currentSize)) }

        var attempt = 0
        while (true) {
            try {
                queue.flushAll()
                _state.update {
                    it.copy(queuedEvents = queue.size, deliveryStatus = DeliveryStatus.Idle)
                }
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                attempt++
                val error = if (e is AnalyticsException) e.error else AnalyticsError.UNKNOWN

                if (!retryPolicy.shouldRetry(attempt)) {
                    _state.update {
                        it.copy(
                            deliveryStatus = DeliveryStatus.Failed(
                                error = error,
                                retryIn = retryPolicy.delayFor(attempt)
                            )
                        )
                    }
                    log(LogLevel.ERROR, "Flush failed after $attempt attempts: ${e.message}")
                    return
                }

                val retryDelay = retryPolicy.delayFor(attempt)
                log(LogLevel.DEBUG, "Flush attempt $attempt failed, retrying in $retryDelay")
                _state.update {
                    it.copy(
                        deliveryStatus = DeliveryStatus.Failed(
                            error = error,
                            retryIn = retryDelay
                        )
                    )
                }
                delay(retryDelay)
            }
        }
    }

    private fun startAutoFlush() {
        scope.launch {
            while (isActive) {
                delay(config.batching.flushInterval)
                if (queue.size > 0) {
                    try {
                        performFlush()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        log(LogLevel.ERROR, "Auto-flush failed: ${e.message}")
                    }
                }
            }
        }

        if (config.batching.flushOnAppBackground) {
            scope.launch(Dispatchers.Main.immediate) {
                ProcessLifecycleOwner.get().lifecycle.addObserver(
                    object : DefaultLifecycleObserver {
                        override fun onStop(owner: LifecycleOwner) {
                            flush()
                        }
                    }
                )
            }
        }
    }

    /**
     * Release all resources. The SDK cannot be used after this call.
     * Call [initialize] again to create a new instance.
     */
    public fun destroy() {
        destroyed = true
        scope.coroutineContext[Job]?.cancelChildren()
        scope.cancel()
        synchronized(Companion) {
            if (instance === this) instance = null
        }
        log(LogLevel.DEBUG, "AnalyticsKit destroyed")
    }

    private fun log(level: LogLevel, message: String) {
        if (config.logging >= level) {
            when (level) {
                LogLevel.ERROR -> Log.e(TAG, message)
                LogLevel.DEBUG -> Log.d(TAG, message)
                LogLevel.VERBOSE -> Log.v(TAG, message)
                LogLevel.NONE -> { /* no-op */ }
            }
        }
    }

    public companion object {

        private const val TAG = "AnalyticsKit"

        @Volatile
        private var instance: AnalyticsKit? = null

        /**
         * Initializes the AnalyticsKit SDK. Must be called before any other method.
         *
         * @param context Application context (will be retained as application context).
         * @param config SDK configuration.
         * @return The initialized [AnalyticsKit] instance.
         * @throws IllegalStateException if already initialized.
         */
        @JvmStatic
        public fun initialize(
            context: Context,
            config: AnalyticsConfig
        ): AnalyticsKit {
            synchronized(this) {
                check(instance == null) {
                    "AnalyticsKit is already initialized. Call destroy() first if you need to reinitialize."
                }
                return AnalyticsKit(
                    config = config,
                    store = EventStore(context.applicationContext),
                    eventDispatcher = EventDispatcher(config)
                ).also { instance = it }
            }
        }

        /**
         * Initializes AnalyticsKit with injected dependencies for unit testing.
         * Use [buildTestInstance] helper from TestHelpers.kt in tests.
         */
        @JvmStatic
        internal fun initializeForTest(
            config: AnalyticsConfig,
            dispatcher: IEventDispatcher,
            store: IEventStore = InMemoryEventStore(),
            coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
        ): AnalyticsKit {
            synchronized(this) {
                // Allow re-initialization in tests (previous instance may have been destroyed)
                instance?.let {
                    if (!it.destroyed) it.destroy()
                }
                return AnalyticsKit(
                    config = config,
                    store = store,
                    eventDispatcher = dispatcher,
                    coroutineDispatcher = coroutineDispatcher
                ).also { instance = it }
            }
        }

        /**
         * Returns the initialized instance.
         *
         * @throws IllegalStateException if [initialize] has not been called.
         */
        @JvmStatic
        public fun getInstance(): AnalyticsKit {
            return checkNotNull(instance) {
                "AnalyticsKit is not initialized. Call AnalyticsKit.initialize() first."
            }
        }
    }
}
