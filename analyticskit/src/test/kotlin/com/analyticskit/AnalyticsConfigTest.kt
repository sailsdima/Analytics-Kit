package com.analyticskit

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class AnalyticsConfigTest {

    @Test
    fun `config with defaults`() {
        val config = AnalyticsConfig(apiKey = "test_key")

        assertEquals("test_key", config.apiKey)
        assertEquals(Environment.PRODUCTION, config.environment)
        assertEquals(LogLevel.NONE, config.logging)
        assertEquals(25, config.batching.maxBatchSize)
        assertEquals(30.seconds, config.batching.flushInterval)
    }

    @Test
    fun `config with custom values`() {
        val config = AnalyticsConfig(
            apiKey = "test_key",
            environment = Environment.STAGING,
            batching = BatchConfig(maxBatchSize = 50, flushInterval = 10.seconds),
            logging = LogLevel.DEBUG
        )

        assertEquals(Environment.STAGING, config.environment)
        assertEquals(50, config.batching.maxBatchSize)
        assertEquals(10.seconds, config.batching.flushInterval)
        assertEquals(LogLevel.DEBUG, config.logging)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `config with blank api key throws`() {
        AnalyticsConfig(apiKey = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `batch config with zero batch size throws`() {
        BatchConfig(maxBatchSize = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `batch config with batch size over 100 throws`() {
        BatchConfig(maxBatchSize = 101)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `batch config with queue smaller than batch throws`() {
        BatchConfig(maxBatchSize = 50, maxQueueSize = 10)
    }

    @Test
    fun `builder produces correct config`() {
        val config = AnalyticsConfig.Builder("key_123")
            .environment(Environment.STAGING)
            .logging(LogLevel.VERBOSE)
            .build()

        assertEquals("key_123", config.apiKey)
        assertEquals(Environment.STAGING, config.environment)
        assertEquals(LogLevel.VERBOSE, config.logging)
    }
}

