package com.analyticskit.internal

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Exponential backoff retry policy for failed event delivery.
 */
internal class RetryPolicy(
    private val maxRetries: Int = 3,
    private val baseDelay: Duration = 1.seconds,
    private val maxDelay: Duration = 30.seconds
) {

    fun shouldRetry(attempt: Int): Boolean = attempt < maxRetries

    fun delayFor(attempt: Int): Duration {
        val delay = baseDelay * (1 shl attempt.coerceAtMost(5))
        return if (delay > maxDelay) maxDelay else delay
    }
}

