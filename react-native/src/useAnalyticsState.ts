/**
 * React hook for observing AnalyticsKit state.
 *
 * @example
 * ```tsx
 * function StatusBar() {
 *   const state = useAnalyticsState();
 *
 *   if (state.deliveryStatus.kind === 'flushing') {
 *     return <Text>Sending {state.deliveryStatus.batchSize} events…</Text>;
 *   }
 *   return <Text>Queued: {state.queuedEvents}</Text>;
 * }
 * ```
 */
import { useState, useEffect } from 'react';
import { onStateChange } from './index';
import type { AnalyticsState } from './types';

const INITIAL_STATE: AnalyticsState = {
  queuedEvents: 0,
  deliveryStatus: { kind: 'idle' },
};

export function useAnalyticsState(): AnalyticsState {
  const [state, setState] = useState<AnalyticsState>(INITIAL_STATE);

  useEffect(() => {
    const unsubscribe = onStateChange(setState);
    return unsubscribe;
  }, []);

  return state;
}
