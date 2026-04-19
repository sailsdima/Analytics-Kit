package com.analyticskit.internal

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists events to SharedPreferences for offline support.
 * In a production SDK, you'd use Room or a file-based store for better performance.
 */
internal class EventStore(context: Context) : IEventStore {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    @Synchronized
    override fun persist(event: InternalEvent) {
        val events = loadAll().toMutableList()
        events.add(event)
        save(events)
    }

    @Synchronized
    override fun count(): Int = loadAll().size

    @Synchronized
    override fun drain(count: Int): List<InternalEvent> {
        val events = loadAll().toMutableList()
        val batch = events.take(count)
        val remaining = events.drop(count)
        save(remaining)
        return batch
    }

    @Synchronized
    override fun drainAll(): List<InternalEvent> {
        val events = loadAll()
        save(emptyList())
        return events
    }

    @Synchronized
    override fun clear() {
        prefs.edit().remove(KEY_EVENTS).apply()
    }

    private fun loadAll(): List<InternalEvent> {
        val json = prefs.getString(KEY_EVENTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                InternalEvent(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    properties = jsonObjectToMap(obj.getJSONObject("properties")),
                    timestamp = obj.getLong("timestamp"),
                    retryCount = obj.optInt("retryCount", 0)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun save(events: List<InternalEvent>) {
        val array = JSONArray()
        events.forEach { event ->
            val obj = JSONObject().apply {
                put("id", event.id)
                put("name", event.name)
                put("properties", JSONObject(event.properties))
                put("timestamp", event.timestamp)
                put("retryCount", event.retryCount)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_EVENTS, array.toString()).apply()
    }

    private fun jsonObjectToMap(obj: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        obj.keys().forEach { key ->
            map[key] = obj.get(key)
        }
        return map
    }

    companion object {
        private const val PREFS_NAME = "analyticskit_events"
        private const val KEY_EVENTS = "queued_events"
    }
}

