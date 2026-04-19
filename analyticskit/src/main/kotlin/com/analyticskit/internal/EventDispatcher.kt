package com.analyticskit.internal

import com.analyticskit.AnalyticsConfig
import com.analyticskit.AnalyticsError
import com.analyticskit.Environment
import com.analyticskit.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Handles network delivery of event batches to the backend.
 */
internal class EventDispatcher(private val config: AnalyticsConfig) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val baseUrl: String
        get() = when (config.environment) {
            Environment.PRODUCTION -> "https://api.analyticskit.io/v1/events"
            Environment.STAGING -> "https://staging-api.analyticskit.io/v1/events"
        }

    /**
     * Sends a batch of events to the backend.
     *
     * @throws AnalyticsException if the request fails
     */
    suspend fun dispatch(events: List<InternalEvent>) {
        if (events.isEmpty()) return

        val payload = buildPayload(events)

        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                response.use {
                    if (!it.isSuccessful) {
                        throw AnalyticsException(
                            error = when (it.code) {
                                401, 403 -> AnalyticsError.INVALID_API_KEY
                                429 -> AnalyticsError.RATE_LIMITED
                                413 -> AnalyticsError.PAYLOAD_TOO_LARGE
                                else -> AnalyticsError.NETWORK_ERROR
                            },
                            message = "HTTP ${it.code}: ${it.message}"
                        )
                    }
                    log("Successfully dispatched ${events.size} events")
                }
            } catch (e: IOException) {
                throw AnalyticsException(
                    error = AnalyticsError.NETWORK_ERROR,
                    message = e.message ?: "Network error"
                )
            }
        }
    }

    private fun buildPayload(events: List<InternalEvent>): String {
        val array = JSONArray()
        events.forEach { event ->
            array.put(JSONObject().apply {
                put("id", event.id)
                put("name", event.name)
                put("properties", JSONObject(event.properties))
                put("timestamp", event.timestamp)
            })
        }
        return JSONObject().apply {
            put("events", array)
            put("sdk_version", SDK_VERSION)
        }.toString()
    }

    private fun log(message: String) {
        if (config.logging >= LogLevel.DEBUG) {
            android.util.Log.d(TAG, message)
        }
    }

    companion object {
        private const val TAG = "AnalyticsKit"
        internal const val SDK_VERSION = "1.0.0"
    }
}

internal class AnalyticsException(
    val error: AnalyticsError,
    override val message: String
) : Exception(message)

