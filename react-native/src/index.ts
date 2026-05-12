/**
 * AnalyticsKit React Native bridge.
 *
 * This module wraps the native Android AnalyticsKit SDK as a React Native
 * NativeModule. The wrapper object is typed against AnalyticsKitAPI so that
 * any method added to the spec but missing here causes a compile error.
 *
 * State observation is exposed via NativeEventEmitter — see useAnalyticsState
 * hook for a React-friendly API.
 */
import { NativeModules, NativeEventEmitter, type EmitterSubscription } from 'react-native';
import type { AnalyticsKitAPI } from './api-spec/AnalyticsKitSpec';
import type { AnalyticsState, Environment } from './types';

const native = NativeModules.AnalyticsKit;

if (!native) {
  throw new Error(
    'AnalyticsKit native module is not linked. ' +
      'Make sure you have rebuilt the app after installing @analyticskit/react-native.'
  );
}

// ---------------------------------------------------------------------------
// Core API — typed against the shared spec
// ---------------------------------------------------------------------------

const AnalyticsKit: AnalyticsKitAPI = {
  initialize: (apiKey: string, environment: Environment): Promise<void> =>
    native.initialize(apiKey, environment),

  track: (name: string, properties?: Record<string, unknown>): void =>
    native.track(name, properties ?? null),

  flush: (): Promise<void> => native.flush(),

  destroy: (): Promise<void> => native.destroy(),

  startObservingState: (): void => native.startObservingState(),

  stopObservingState: (): void => native.stopObservingState(),
};

// ---------------------------------------------------------------------------
// Event emitter for state observation
// ---------------------------------------------------------------------------

const emitter = new NativeEventEmitter(native);

const STATE_CHANGED_EVENT = 'AnalyticsKitStateChanged';

/**
 * Subscribe to AnalyticsKit state changes.
 *
 * Automatically calls startObservingState() on the native side.
 * Returns an unsubscribe function that stops observation and removes
 * the listener.
 *
 * @example
 * ```ts
 * const unsubscribe = onStateChange((state) => {
 *   console.log(`Queue: ${state.queuedEvents}`);
 * });
 *
 * // Later:
 * unsubscribe();
 * ```
 */
export function onStateChange(
  listener: (state: AnalyticsState) => void
): () => void {
  AnalyticsKit.startObservingState();
  const subscription: EmitterSubscription = emitter.addListener(
    STATE_CHANGED_EVENT,
    listener
  );

  return () => {
    subscription.remove();
    AnalyticsKit.stopObservingState();
  };
}

// ---------------------------------------------------------------------------
// Exports
// ---------------------------------------------------------------------------

export default AnalyticsKit;
export type { AnalyticsKitAPI } from './api-spec/AnalyticsKitSpec';
export type {
  AnalyticsState,
  DeliveryStatus,
  AnalyticsError,
  Environment,
  TrackOptions,
} from './types';

