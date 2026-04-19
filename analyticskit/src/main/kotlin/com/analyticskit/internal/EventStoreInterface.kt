package com.analyticskit.internal

internal interface IEventStore {
    fun persist(event: InternalEvent)
    fun count(): Int
    fun drain(count: Int): List<InternalEvent>
    fun drainAll(): List<InternalEvent>
    fun clear()
}
