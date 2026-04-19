package com.analyticskit

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The target environment for the SDK.
 */
public enum class Environment {
    PRODUCTION,
    STAGING
}

/**
 * Controls the SDK's internal logging verbosity.
 */
public enum class LogLevel {
    NONE,
    ERROR,
    DEBUG,
    VERBOSE
}

/**
 * Configuration for event batching behavior.
 *
 * @property maxBatchSize Maximum number of events per batch (1–100).
 * @property flushInterval How often to automatically flush queued events.
 * @property maxQueueSize Maximum number of events to hold in the queue before dropping oldest.
 * @property persistOfflineEvents Whether to persist events to disk when offline.
 * @property flushOnAppBackground Whether to flush when the app moves to the background.
 *   Registers a [ProcessLifecycleOwner] observer. Set to false if you want to manage
 *   lifecycle integration yourself.
 */
public data class BatchConfig(
    val maxBatchSize: Int = 25,
    val flushInterval: Duration = 30.seconds,
    val maxQueueSize: Int = 1000,
    val persistOfflineEvents: Boolean = true,
    val flushOnAppBackground: Boolean = true
) {
    init {
        require(maxBatchSize in 1..100) { "Batch size must be between 1 and 100" }
        require(maxQueueSize >= maxBatchSize) { "Queue size must be >= batch size" }
    }
}

/**
 * Configuration for the AnalyticsKit SDK.
 *
 * @property apiKey Your API key. Must not be blank.
 * @property environment Target environment.
 * @property batching Batching configuration.
 * @property logging Internal log verbosity.
 * @property eventInterceptor Optional interceptor for enriching/filtering events before delivery.
 */
public data class AnalyticsConfig(
    val apiKey: String,
    val environment: Environment = Environment.PRODUCTION,
    val batching: BatchConfig = BatchConfig(),
    val logging: LogLevel = LogLevel.NONE,
    val eventInterceptor: EventInterceptor? = null
) {
    init {
        require(apiKey.isNotBlank()) { "API key must not be blank" }
    }

    /** Builder for Java interop. */
    public class Builder(private val apiKey: String) {
        private var environment: Environment = Environment.PRODUCTION
        private var batching: BatchConfig = BatchConfig()
        private var logging: LogLevel = LogLevel.NONE
        private var eventInterceptor: EventInterceptor? = null

        public fun environment(env: Environment): Builder = apply { this.environment = env }
        public fun batching(config: BatchConfig): Builder = apply { this.batching = config }
        public fun logging(level: LogLevel): Builder = apply { this.logging = level }
        public fun eventInterceptor(interceptor: EventInterceptor?): Builder = apply { this.eventInterceptor = interceptor }

        public fun build(): AnalyticsConfig = AnalyticsConfig(
            apiKey = apiKey,
            environment = environment,
            batching = batching,
            logging = logging,
            eventInterceptor = eventInterceptor
        )
    }
}
