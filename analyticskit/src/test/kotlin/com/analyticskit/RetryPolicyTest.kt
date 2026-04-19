package com.analyticskit

import com.analyticskit.internal.RetryPolicy
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class RetryPolicyTest {

    @Test
    fun `backoff delay increases exponentially`() {
        val policy = RetryPolicy(baseDelay = 1.seconds, maxDelay = 30.seconds)

        assertThat(policy.delayFor(attempt = 1)).isEqualTo(2.seconds)
        assertThat(policy.delayFor(attempt = 2)).isEqualTo(4.seconds)
        assertThat(policy.delayFor(attempt = 3)).isEqualTo(8.seconds)
    }

    @Test
    fun `delay caps at maximum`() {
        val policy = RetryPolicy(baseDelay = 1.seconds, maxDelay = 5.seconds)
        assertThat(policy.delayFor(attempt = 10)).isEqualTo(5.seconds)
    }

    @Test
    fun `shouldRetry is false beyond max attempts`() {
        val policy = RetryPolicy(maxRetries = 3)

        assertThat(policy.shouldRetry(attempt = 1)).isTrue()
        assertThat(policy.shouldRetry(attempt = 3)).isTrue()
        assertThat(policy.shouldRetry(attempt = 4)).isFalse()
    }
}
