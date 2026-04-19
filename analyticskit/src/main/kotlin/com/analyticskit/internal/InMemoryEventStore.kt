package com.analyticskit.internal

/**
 * In-memory event store used for testing without Android context.
 */
internal class InMemoryEventStore : IEventStore {

    private val events = mutableListOf<InternalEvent>()

    @Synchronized
    override fun persist(event: InternalEvent) {
        events.add(event)
    }

    @Synchronized
    override fun count(): Int = events.size

    @Synchronized
    override fun drain(count: Int): List<InternalEvent> {
        val batch = events.take(count)
        repeat(batch.size) { events.removeAt(0) }
        return batch
    }

    @Synchronized
    override fun drainAll(): List<InternalEvent> {
        val all = events.toList()
        events.clear()
        return all
    }

    @Synchronized
    override fun clear() {
        events.clear()
    }
}
