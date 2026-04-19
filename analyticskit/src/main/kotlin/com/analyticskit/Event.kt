package com.analyticskit

/**
 * Represents an analytics event to be tracked.
 *
 * @property name The event name. Must not be blank.
 * @property properties Optional key-value pairs associated with the event.
 * @property timestamp The time the event occurred, in milliseconds since epoch.
 */
public data class Event(
    val name: String,
    val properties: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
) {
    init {
        require(name.isNotBlank()) { "Event name must not be blank" }
    }
}
