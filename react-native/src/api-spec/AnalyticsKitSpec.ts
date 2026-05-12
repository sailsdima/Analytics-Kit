/**
 * AnalyticsKit API specification.
 *
 * This interface is the single source of truth for API parity between
 * the native Android SDK and the React Native bridge. Both sides validate
 * against it:
 *
 * - TypeScript: the wrapper object in index.ts must satisfy this interface
 *   (compiler enforced).
 * - Kotlin: AnalyticsKitModuleApiParityTest verifies that every method
 *   listed here has a corresponding @ReactMethod in the bridge module.
 */
export interface AnalyticsKitAPI {
  /**
   * Initializes the native AnalyticsKit SDK.
   * Must be called before any other method.
   */
  initialize(
    apiKey: string,
    environment: 'production' | 'staging'
  ): Promise<void>;

  /**
   * Tracks an event with a name and optional properties.
   * Fire-and-forget — does not return a promise.
   */
  track(name: string, properties?: Record<string, unknown>): void;

  /**
   * Forces immediate delivery of all queued events.
   * The promise resolves after delivery completes (not when the flush
   * is merely enqueued).
   */
  flush(): Promise<void>;

  /**
   * Releases all SDK resources. The SDK cannot be used after this call.
   */
  destroy(): Promise<void>;

  /**
   * Begins emitting 'AnalyticsKitStateChanged' events via
   * NativeEventEmitter. Call stopObservingState() to cancel.
   */
  startObservingState(): void;

  /**
   * Stops emitting state change events.
   */
  stopObservingState(): void;
}

