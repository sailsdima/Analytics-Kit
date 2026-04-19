package com.sailsdima.analyticskit.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.analyticskit.AnalyticsKit
import com.analyticskit.AnalyticsState
import com.analyticskit.DeliveryStatus
import com.analyticskit.Event

class MainActivity : ComponentActivity() {
    private val analytics by lazy { AnalyticsKit.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics.track("screen_viewed", mapOf("screen" to "main"))

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by analytics.state.collectAsState()
                    SampleScreen(
                        state = state,
                        onTrackSimple = {
                            analytics.track("button_clicked", mapOf("button" to "simple"))
                        },
                        onTrackStructured = {
                            analytics.track(
                                Event(
                                    name = "item_added_to_cart",
                                    properties = mapOf(
                                        "item_id" to "SKU-1234",
                                        "price" to 29.99,
                                        "currency" to "USD"
                                    )
                                )
                            )
                        },
                        onFlush = { analytics.flush() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleScreen(
    state: AnalyticsState,
    onTrackSimple: () -> Unit,
    onTrackStructured: () -> Unit,
    onFlush: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "AnalyticsKit Sample", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "SDK State", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Queued events: ${state.queuedEvents}")
                Text(text = "Status: ${state.deliveryStatus.displayName()}")
            }
        }

        Button(onClick = onTrackSimple, modifier = Modifier.fillMaxWidth()) {
            Text("Track Simple Event")
        }
        Button(onClick = onTrackStructured, modifier = Modifier.fillMaxWidth()) {
            Text("Track Structured Event")
        }
        Button(onClick = onFlush, modifier = Modifier.fillMaxWidth()) {
            Text("Flush Now")
        }
    }
}

private fun DeliveryStatus.displayName(): String = when (this) {
    DeliveryStatus.Idle -> "Idle"
    is DeliveryStatus.Flushing -> "Flushing (${batchSize} events)"
    is DeliveryStatus.Failed -> "Failed: ${error.name} (retry in ${retryIn})"
}
