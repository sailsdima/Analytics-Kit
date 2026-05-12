# @analyticskit/react-native

React Native bridge for [AnalyticsKit](https://github.com/sailsdima/Analytics-Kit) — a modern, lightweight analytics SDK for Android.

This package is a **thin wrapper** over the native Android SDK. All business logic (batching, retry, persistence, offline queuing) lives in the native layer. The bridge handles type conversion, Promise resolution, and `StateFlow` → `NativeEventEmitter` bridging.

## Installation

```bash
npm install @analyticskit/react-native
# or
yarn add @analyticskit/react-native
```

### Android setup

Add `AnalyticsKitPackage` to your `MainApplication`:

```java
import com.analyticskit.rn.AnalyticsKitPackage;

@Override
protected List<ReactPackage> getPackages() {
    List<ReactPackage> packages = new PackageList(this).getPackages();
    packages.add(new AnalyticsKitPackage());
    return packages;
}
```

## Usage

```typescript
import AnalyticsKit, { onStateChange } from '@analyticskit/react-native';

// Initialize
await AnalyticsKit.initialize('your_api_key', 'production');

// Track events
AnalyticsKit.track('screen_viewed', { screen: 'home' });
AnalyticsKit.track('item_added_to_cart', {
  item_id: 'SKU-1234',
  price: 29.99,
  currency: 'USD',
});

// Observe state
const unsubscribe = onStateChange((state) => {
  console.log(`Queue: ${state.queuedEvents}, Status: ${state.deliveryStatus.kind}`);
});

// Flush manually
await AnalyticsKit.flush();

// Cleanup
unsubscribe();
await AnalyticsKit.destroy();
```

### React hook

```tsx
import { useAnalyticsState } from '@analyticskit/react-native/lib/useAnalyticsState';

function StatusBar() {
  const state = useAnalyticsState();

  switch (state.deliveryStatus.kind) {
    case 'flushing':
      return <Text>Sending {state.deliveryStatus.batchSize} events…</Text>;
    case 'failed':
      return <Text>Error: {state.deliveryStatus.error}</Text>;
    default:
      return <Text>Queued: {state.queuedEvents}</Text>;
  }
}
```

## Type safety

All types mirror the Kotlin SDK's public API:

| Kotlin | TypeScript |
|--------|-----------|
| `DeliveryStatus` (sealed interface) | Discriminated union with `kind` field |
| `AnalyticsError` (enum) | String literal union |
| `Duration` | `number` in milliseconds (field name includes unit) |
| `null` | `null` (not `undefined`) |

## API parity

The API spec lives in `src/api-spec/AnalyticsKitSpec.ts`. Both sides validate against it:

- **TypeScript**: the wrapper in `index.ts` must satisfy `AnalyticsKitAPI` (compiler enforced).
- **Kotlin**: `AnalyticsKitModuleApiParityTest` verifies every spec method has a `@ReactMethod`.

## Versioning

This package shares the same version number as the native AnalyticsKit SDK. When the native SDK bumps to `1.2.0`, this package is also `1.2.0`.

## License

MIT
