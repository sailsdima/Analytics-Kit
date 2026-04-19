# AnalyticsKit

## Architecture

```
analyticskit/          → The SDK library module
  ├── AnalyticsKit     → Public entry point (singleton + instance)
  ├── AnalyticsConfig  → Configuration with defaults & Builder
  ├── Event            → Public event model
  ├── AnalyticsState   → Observable pipeline state (StateFlow)
  └── internal/        → Batching, persistence, networking, retry
app/                   → Sample app demonstrating SDK usage
```

## Quick Start

```kotlin
// 1. Initialize (in Application.onCreate)
val analytics = AnalyticsKit.initialize(
    context = applicationContext,
    config = AnalyticsConfig(
        apiKey = "your_api_key",
        batching = BatchConfig(maxBatchSize = 25, flushInterval = 30.seconds)
    )
)

// 2. Track events
analytics.track("screen_viewed", mapOf("screen" to "home"))

// 3. Observe state
analytics.state.collect { state ->
    println("Queued: ${state.queuedEvents}, Status: ${state.deliveryStatus}")
}

// 4. Force flush
analytics.flush()
```

## Key Principles Demonstrated

1. **API-first design** — public interface designed before implementation
2. **Hybrid singleton** — explicit init + convenient `getInstance()`
3. **Configuration as data** — `data class` with defaults + Java Builder
4. **Reactive state** — `StateFlow` for pipeline observability
5. **Visibility control** — `internal` keyword + `explicitApi()` mode
6. **Dependency isolation** — `implementation` vs `api`, no leaked deps
7. **Thread safety** — `SupervisorJob`, structured concurrency, retry with backoff

## Building

```bash
./gradlew :analyticskit:build
./gradlew :analyticskit:test
```

## License

MIT

