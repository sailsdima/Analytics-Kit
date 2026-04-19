package com.analyticskit

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class AutoFlushTest {

    @Test
    fun `auto-flush fires after the configured interval`() = runTest {
        val analytics = buildTestInstance(
            flushInterval = 30.seconds,
            flushOnAppBackground = false
        )

        repeat(3) { analytics.track("event_$it") }
        runCurrent() // let track() coroutines complete without advancing virtual clock

        assertThat(analytics.state.value.queuedEvents).isEqualTo(3)

        advanceTimeBy(30_001) // jump the virtual clock past the interval
        runCurrent()          // run the single auto-flush iteration (not advanceUntilIdle — that loops forever)

        assertThat(analytics.state.value.queuedEvents).isEqualTo(0)
        analytics.destroy()
    }

    @Test
    fun `auto-flush loop survives a single network failure`() = runTest {
        val fakeDispatcher = FakeEventDispatcher(failOnFirstCall = true)
        val analytics = buildTestInstance(dispatcher = fakeDispatcher)

        analytics.track("will_fail_first_attempt")
        runCurrent()

        // First auto-flush fails — the loop must not die
        advanceTimeBy(30_001)
        runCurrent() // auto-flush fires, dispatch fails, performFlush delays retryDelay

        // Second auto-flush window: advance past retry delay + next interval
        advanceTimeBy(30_001)
        runCurrent() // retry delay expires → dispatch succeeds; loop continues

        assertThat(analytics.state.value.queuedEvents).isEqualTo(0)
        analytics.destroy()
    }
}
