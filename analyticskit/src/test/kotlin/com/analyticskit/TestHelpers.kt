package com.analyticskit

import com.analyticskit.internal.AnalyticsException
import com.analyticskit.internal.IEventDispatcher
import com.analyticskit.internal.InternalEvent
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun TestScope.buildTestInstance(
    flushInterval: Duration = 30.seconds,
    flushOnAppBackground: Boolean = false,
    failNextFlush: Boolean = false,
    dispatcher: FakeEventDispatcher = FakeEventDispatcher(failOnFirstCall = failNextFlush)
): AnalyticsKit = AnalyticsKit.initializeForTest(
    config = AnalyticsConfig(
        apiKey = "test_key",
        environment = Environment.STAGING,
        batching = BatchConfig(
            flushInterval = flushInterval,
            flushOnAppBackground = flushOnAppBackground
        )
    ),
    dispatcher = dispatcher,
    coroutineDispatcher = StandardTestDispatcher(testScheduler)
)

internal class FakeEventDispatcher(
    private val failOnFirstCall: Boolean = false
) : IEventDispatcher {
    val batches = mutableListOf<List<InternalEvent>>()
    private var callCount = 0

    override suspend fun dispatch(events: List<InternalEvent>) {
        callCount++
        if (failOnFirstCall && callCount == 1) {
            throw AnalyticsException(AnalyticsError.NETWORK_ERROR, "Simulated network failure")
        }
        batches.add(events)
    }
}
