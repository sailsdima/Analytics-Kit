# AnalyticsKit SDK ProGuard Rules
# Keep all public API classes
-keep class com.analyticskit.AnalyticsKit { *; }
-keep class com.analyticskit.AnalyticsConfig { *; }
-keep class com.analyticskit.AnalyticsConfig$Builder { *; }
-keep class com.analyticskit.BatchConfig { *; }
-keep class com.analyticskit.Event { *; }
-keep class com.analyticskit.AnalyticsState { *; }
-keep class com.analyticskit.DeliveryStatus { *; }
-keep class com.analyticskit.DeliveryStatus$* { *; }
-keep class com.analyticskit.AnalyticsError { *; }
-keep class com.analyticskit.Environment { *; }
-keep class com.analyticskit.LogLevel { *; }
-keep class com.analyticskit.EventInterceptor { *; }

