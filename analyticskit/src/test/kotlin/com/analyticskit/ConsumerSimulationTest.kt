package com.analyticskit

// Rule: only public API imports. No com.analyticskit.internal.*

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ConsumerSimulationTest {

    @After
    fun tearDown() {
        runCatching { AnalyticsKit.getInstance().destroy() }
    }

    @Test
    fun `consumer initializes SDK and tracks events`() {
        val analytics = AnalyticsKit.initialize(
            context = ApplicationProvider.getApplicationContext(),
            config = AnalyticsConfig(
                apiKey = "key_test_abc",
                environment = Environment.STAGING,
                batching = BatchConfig(flushOnAppBackground = false),
                logging = LogLevel.DEBUG
            )
        )

        analytics.track("screen_viewed", mapOf("screen" to "home"))
        analytics.track(
            Event(
                name = "item_added_to_cart",
                properties = mapOf("item_id" to "SKU-9988", "price" to 49.99)
            )
        )
        Thread.sleep(300) // let SDK's Default dispatcher coroutines complete

        assertThat(analytics.state.value.queuedEvents).isEqualTo(2)
    }

    @Test
    fun `EventInterceptor filters events before delivery`() {
        val analytics = AnalyticsKit.initialize(
            context = ApplicationProvider.getApplicationContext(),
            config = AnalyticsConfig(
                apiKey = "key_test",
                batching = BatchConfig(flushOnAppBackground = false),
                eventInterceptor = EventInterceptor { events ->
                    events.filter { it.name != "debug_noise" }
                }
            )
        )

        analytics.track("purchase_completed")
        analytics.track("debug_noise") // should be filtered
        Thread.sleep(300)

        assertThat(analytics.state.value.queuedEvents).isEqualTo(1)
    }

    @Test
    fun `double initialization throws`() {
        AnalyticsKit.initialize(
            context = ApplicationProvider.getApplicationContext(),
            config = AnalyticsConfig(apiKey = "first")
        )

        assertThrows(IllegalStateException::class.java) {
            AnalyticsKit.initialize(
                context = ApplicationProvider.getApplicationContext(),
                config = AnalyticsConfig(apiKey = "second")
            )
        }
    }

    @Test
    fun `track after destroy throws`() {
        val analytics = AnalyticsKit.initialize(
            context = ApplicationProvider.getApplicationContext(),
            config = AnalyticsConfig(apiKey = "key")
        )
        analytics.destroy()

        assertThrows(IllegalStateException::class.java) {
            analytics.track("post_destroy_event")
        }
    }

    @Test
    fun `Java Builder produces equivalent config to Kotlin DSL`() {
        val kotlinStyle = AnalyticsConfig(
            apiKey = "key",
            environment = Environment.STAGING,
            logging = LogLevel.DEBUG
        )
        val javaStyle = AnalyticsConfig.Builder("key")
            .environment(Environment.STAGING)
            .logging(LogLevel.DEBUG)
            .build()

        assertThat(javaStyle.environment).isEqualTo(kotlinStyle.environment)
        assertThat(javaStyle.logging).isEqualTo(kotlinStyle.logging)
    }
}
