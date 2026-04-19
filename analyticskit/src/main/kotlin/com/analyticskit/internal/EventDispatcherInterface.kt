package com.analyticskit.internal

/**
 * Abstraction over event delivery. Allows swapping implementations
 * (HTTP, fake for tests) without changing [EventQueue].
 */
internal interface IEventDispatcher {
    suspend fun dispatch(events: List<InternalEvent>)
}
