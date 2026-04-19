package com.sailsdima.analyticskit.sample

import android.app.Application
import com.analyticskit.AnalyticsConfig
import com.analyticskit.AnalyticsKit
import com.analyticskit.BatchConfig
import com.analyticskit.Environment
import com.analyticskit.LogLevel
import kotlin.time.Duration.Companion.seconds

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AnalyticsKit.initialize(
            context = this,
            config = AnalyticsConfig(
                apiKey = "sample_api_key_12345",
                environment = Environment.STAGING,
                batching = BatchConfig(
                    maxBatchSize = 10,
                    flushInterval = 15.seconds
                ),
                logging = LogLevel.VERBOSE
            )
        )
    }
}

