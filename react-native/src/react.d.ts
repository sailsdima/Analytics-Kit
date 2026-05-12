/**
 * Minimal type declarations for React hooks used by useAnalyticsState.
 * In a real RN project, these come from @types/react.
 */
declare module 'react' {
  export function useState<T>(initialState: T): [T, (value: T) => void];
  export function useEffect(effect: () => void | (() => void), deps?: unknown[]): void;
}
