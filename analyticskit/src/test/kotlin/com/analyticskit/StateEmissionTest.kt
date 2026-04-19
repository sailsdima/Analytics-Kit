package com.analyticskit

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class StateEmissionTest {

    @Test
    fun `flush transitions through Flushing then settles at Idle`() = runTest {
        val analytics = buildTestInstance()
        analytics.track("purchase_completed")
        runCurrent()

        analytics.state.test {
            analytics.flush()
            runCurrent()

            awaitItem()
            val flushing = awaitItem()
            assertThat(flushing.deliveryStatus).isInstanceOf(DeliveryStatus.Flushing::class.java)

            val idle = awaitItem()
            assertThat(idle.deliveryStatus).isEqualTo(DeliveryStatus.Idle)
            assertThat(idle.queuedEvents).isEqualTo(0)

            cancelAndIgnoreRemainingEvents()
        }

        analytics.destroy()
    }

    @Test
    fun `failed flush emits DeliveryStatus Failed with the correct error`() = runTest {
        val analytics = buildTestInstance(failNextFlush = true)
        analytics.track("purchase_completed")
        runCurrent()

        analytics.state.test {
            analytics.flush()
            runCurrent()

            awaitItem()
            skipItems(1)

            val failed = awaitItem()
            assertThat(failed.deliveryStatus).isInstanceOf(DeliveryStatus.Failed::class.java)

            val status = failed.deliveryStatus as DeliveryStatus.Failed
            assertThat(status.error).isEqualTo(AnalyticsError.NETWORK_ERROR)

            cancelAndIgnoreRemainingEvents()
        }

        analytics.destroy()
    }
}
