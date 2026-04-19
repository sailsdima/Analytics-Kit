package com.analyticskit.internal

import com.analyticskit.Event

/**
 * Internal representation of an event with additional metadata.
 */
internal data class InternalEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val properties: Map<String, Any>,
    val timestamp: Long,
    val retryCount: Int = 0
)

internal fun Event.toInternal(): InternalEvent = InternalEvent(
    name = name,
    properties = properties,
    timestamp = timestamp
)
