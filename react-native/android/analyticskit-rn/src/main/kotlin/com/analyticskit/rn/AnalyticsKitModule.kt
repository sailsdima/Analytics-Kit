package com.analyticskit.rn

import com.analyticskit.AnalyticsConfig
import com.analyticskit.AnalyticsKit
import com.analyticskit.AnalyticsState
import com.analyticskit.DeliveryStatus
import com.analyticskit.Environment
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * React Native bridge for [AnalyticsKit].
 *
 * This is a thin wrapper — all business logic (batching, retry, persistence)
 * lives in the native SDK. The bridge handles:
 * - Type conversion between JS (ReadableMap) and Kotlin (Map<String, Any>)
 * - Promise resolution for async operations
 * - StateFlow → NativeEventEmitter bridging
 *
 * Threading: every @ReactMethod is invoked on the native modules thread.
 * AnalyticsKit.track() is inherently non-blocking (it dispatches into its own
 * coroutine scope), so no additional threading is needed for fire-and-forget calls.
 * For flush(), the bridge launches its own coroutine so the Promise resolves only
 * after delivery completes — not when the flush is merely enqueued.
 */
class AnalyticsKitModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    @Volatile private var stateJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun getName(): String = "AnalyticsKit"

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @ReactMethod
    fun initialize(apiKey: String, environment: String, promise: Promise) {
        try {
            val env = when (environment) {
                "staging" -> Environment.STAGING
                else -> Environment.PRODUCTION
            }
            AnalyticsKit.initialize(
                context = reactApplicationContext,
                config = AnalyticsConfig(
                    apiKey = apiKey,
                    environment = env
                )
            )
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("INIT_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun destroy(promise: Promise) {
        try {
            AnalyticsKit.getInstance().destroy()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("DESTROY_ERROR", e.message, e)
        }
    }

    // -----------------------------------------------------------------------
    // Event tracking
    // -----------------------------------------------------------------------

    /**
     * Tracks an event. Fire-and-forget — no promise, no waiting.
     *
     * The `?: Unit` fallback preserves original value types from ReadableMap.
     * Using `?: ""` would silently coerce nulls to empty strings, corrupting
     * numeric or boolean properties (e.g. `price: 29.99` → `"29.99"`).
     */
    @ReactMethod
    fun track(name: String, properties: ReadableMap?) {
        val props = properties?.toHashMap()
            ?.mapValues { it.value ?: Unit }
            ?: emptyMap()
        AnalyticsKit.getInstance().track(name, props)
    }

    // -----------------------------------------------------------------------
    // Flush
    // -----------------------------------------------------------------------

    /**
     * Forces immediate delivery of all queued events.
     *
     * The bridge launches its own coroutine and calls [AnalyticsKit.flushAsync]
     * — a suspend function that completes only after delivery finishes. Calling
     * the public [AnalyticsKit.flush] instead would resolve the Promise
     * immediately (since flush() uses scope.launch {} internally), before events
     * are actually delivered.
     */
    @ReactMethod
    fun flush(promise: Promise) {
        scope.launch {
            try {
                AnalyticsKit.getInstance().flushAsync()
                promise.resolve(null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                promise.reject("FLUSH_ERROR", e.message, e)
            }
        }
    }

    // -----------------------------------------------------------------------
    // State observation
    // -----------------------------------------------------------------------

    /**
     * Begins collecting from [AnalyticsKit.state] and emitting
     * 'AnalyticsKitStateChanged' events to JS via [NativeEventEmitter].
     *
     * Explicit start/stop gives JS consumers lifecycle control. Auto-starting
     * in the module constructor would be the same anti-pattern as silently
     * registering ProcessLifecycleOwner (avoided in the native SDK's Part 1).
     */
    @ReactMethod
    fun startObservingState() {
        stateJob?.cancel()
        stateJob = scope.launch {
            AnalyticsKit.getInstance().state.collect { state ->
                val params = Arguments.createMap().apply {
                    putInt("queuedEvents", state.queuedEvents)
                    putString("deliveryStatus", state.deliveryStatus.toJsString())
                    state.deliveryStatus.toJsExtras(this)
                }
                reactApplicationContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit("AnalyticsKitStateChanged", params)
            }
        }
    }

    @ReactMethod
    fun stopObservingState() {
        stateJob?.cancel()
        stateJob = null
    }

    // -----------------------------------------------------------------------
    // Cleanup
    // -----------------------------------------------------------------------

    override fun onCatalystInstanceDestroy() {
        scope.cancel()
    }
}

// ---------------------------------------------------------------------------
// Extension functions: flatten sealed interface into JS-friendly strings
// ---------------------------------------------------------------------------

private fun DeliveryStatus.toJsString(): String = when (this) {
    is DeliveryStatus.Idle -> "idle"
    is DeliveryStatus.Flushing -> "flushing"
    is DeliveryStatus.Failed -> "failed"
}

/**
 * Appends variant-specific fields to the JS event map.
 * Duration is converted to milliseconds — the field name includes the unit
 * (retryInMs) because JS consumers can't hover over a Duration type.
 */
private fun DeliveryStatus.toJsExtras(map: WritableMap) {
    when (this) {
        is DeliveryStatus.Flushing -> map.putInt("batchSize", batchSize)
        is DeliveryStatus.Failed -> {
            map.putString("error", error.name)
            map.putDouble("retryInMs", retryIn.inWholeMilliseconds.toDouble())
        }
        is DeliveryStatus.Idle -> { /* no extras */ }
    }
}

