package com.analyticskit

import kotlin.time.Duration

/**
 * Represents the current state of the analytics pipeline.
 */
public data class AnalyticsState(
    val queuedEvents: Int,
    val deliveryStatus: DeliveryStatus
)

/**
 * Represents the delivery status of the analytics pipeline.
 */
public sealed interface DeliveryStatus {

    /** No flush in progress. */
    public data object Idle : DeliveryStatus

    /** Events are being delivered to the backend. */
    public data class Flushing(val batchSize: Int) : DeliveryStatus

    /** Last flush failed. Will retry after [retryIn]. */
    public data class Failed(
        val error: AnalyticsError,
        val retryIn: Duration
    ) : DeliveryStatus
}

/**
 * Errors that can occur during event delivery.
 */
public enum class AnalyticsError {
    NETWORK_ERROR,
    INVALID_API_KEY,
    RATE_LIMITED,
    PAYLOAD_TOO_LARGE,
    UNKNOWN
}
