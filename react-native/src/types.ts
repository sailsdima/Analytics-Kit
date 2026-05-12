/**
 * TypeScript types mirroring the Kotlin public API of AnalyticsKit.
 *
 * These types map Kotlin constructs to TypeScript equivalents:
 * - Sealed interface (DeliveryStatus) → discriminated union with `kind` field
 * - Kotlin Duration → number in milliseconds (field name includes unit: `retryInMs`)
 * - Kotlin enum (AnalyticsError) → string literal union
 * - Kotlin null → TypeScript null (not undefined — React Native bridge convention)
 */

// ---------------------------------------------------------------------------
// DeliveryStatus — mirrors com.analyticskit.DeliveryStatus sealed interface
// ---------------------------------------------------------------------------

export type DeliveryStatus =
  | { kind: 'idle' }
  | { kind: 'flushing'; batchSize: number }
  | { kind: 'failed'; error: AnalyticsError; retryInMs: number };

// ---------------------------------------------------------------------------
// AnalyticsError — mirrors com.analyticskit.AnalyticsError enum
// ---------------------------------------------------------------------------

export type AnalyticsError =
  | 'NETWORK_ERROR'
  | 'INVALID_API_KEY'
  | 'RATE_LIMITED'
  | 'PAYLOAD_TOO_LARGE'
  | 'UNKNOWN';

// ---------------------------------------------------------------------------
// AnalyticsState — mirrors com.analyticskit.AnalyticsState data class
// ---------------------------------------------------------------------------

export interface AnalyticsState {
  queuedEvents: number;
  deliveryStatus: DeliveryStatus;
}

// ---------------------------------------------------------------------------
// Configuration types
// ---------------------------------------------------------------------------

export type Environment = 'production' | 'staging';

export interface TrackOptions {
  properties: Record<string, unknown> | null;
}
