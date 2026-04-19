package com.analyticskit.internal

import com.analyticskit.BatchConfig
import com.analyticskit.LogLevel

/**
 * Manages the event queue — enqueuing, batching, and triggering flushes.
 */
internal class EventQueue(
    private val config: BatchConfig,
    private val store: EventStore,
    private val dispatcher: EventDispatcher,
    private val logLevel: LogLevel = LogLevel.NONE
) {

    val size: Int
        get() = store.count()

    fun enqueue(event: InternalEvent) {
        // Drop oldest events if queue is full
        if (store.count() >= config.maxQueueSize) {
            store.drain(1) // Drop oldest
            log("Queue full, dropping oldest event")
        }
        store.persist(event)
        log("Event enqueued: ${event.name} (queue size: ${store.count()})")
    }

    suspend fun flushBatch(): Int {
        val batch = store.drain(config.maxBatchSize)
        if (batch.isEmpty()) return 0
        dispatcher.dispatch(batch)
        return batch.size
    }

    suspend fun flushAll(): Int {
        var totalFlushed = 0
        while (store.count() > 0) {
            totalFlushed += flushBatch()
        }
        return totalFlushed
    }

    fun clear() {
        store.clear()
    }

    private fun log(message: String) {
        if (logLevel >= LogLevel.DEBUG) {
            android.util.Log.d("AnalyticsKit", message)
        }
    }
}

