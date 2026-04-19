# AnalyticsKit SDK ProGuard Rules

# Public entry point and configuration
-keep public class com.analyticskit.AnalyticsKit { public *; }
-keep public class com.analyticskit.AnalyticsConfig { *; }
-keep public class com.analyticskit.AnalyticsConfig$Builder { *; }
-keep public class com.analyticskit.BatchConfig { *; }
-keep public class com.analyticskit.Event { *; }
-keep public class com.analyticskit.AnalyticsState { *; }

# Sealed interface and enum classes — R8 removes unused variants by default
-keep class com.analyticskit.DeliveryStatus { *; }
-keep class com.analyticskit.DeliveryStatus$* { *; }
-keep class com.analyticskit.AnalyticsError { *; }
-keep class com.analyticskit.Environment { *; }
-keep class com.analyticskit.LogLevel { *; }

# EventInterceptor — consumers implement this as a lambda or anonymous class
-keep interface com.analyticskit.EventInterceptor { *; }

# Kotlin data class synthetic methods: copy(), componentN(), equals(), hashCode()
-keepclassmembers class com.analyticskit.** {
    synthetic <methods>;
}
