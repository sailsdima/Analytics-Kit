package com.analyticskit

import com.analyticskit.internal.RetryPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class RetryPolicyTest {

    @Test
    fun `should retry within max attempts`() {
        val policy = RetryPolicy(maxRetries = 3)

        assertTrue(policy.shouldRetry(0))
        assertTrue(policy.shouldRetry(1))
        assertTrue(policy.shouldRetry(2))
        assertFalse(policy.shouldRetry(3))
    }

    @Test
    fun `delay increases exponentially`() {
        val policy = RetryPolicy(baseDelay = 1.seconds, maxDelay = 30.seconds)

        assertEquals(2.seconds, policy.delayFor(1))
        assertEquals(4.seconds, policy.delayFor(2))
        assertEquals(8.seconds, policy.delayFor(3))
    }

    @Test
    fun `delay caps at max`() {
        val policy = RetryPolicy(baseDelay = 1.seconds, maxDelay = 5.seconds)

        assertEquals(5.seconds, policy.delayFor(5))
    }
}
