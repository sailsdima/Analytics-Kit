package com.analyticskit

/**
 * Functional interface for intercepting events before delivery.
 *
 * Use this to enrich, filter, or transform events before they are sent to the backend.
 */
public fun interface EventInterceptor {
    /**
     * Intercepts a list of events. Return the modified list.
     * Returning an empty list will drop all events in the batch.
     */
    public suspend fun intercept(events: List<Event>): List<Event>
}
