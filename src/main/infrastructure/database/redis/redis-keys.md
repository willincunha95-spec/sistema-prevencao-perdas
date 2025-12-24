# Redis Key Strategy

This document defines the key patterns used in Redis for caching and fast access.

## Key Namespaces

### Caching
*   `registers:cache:{id}`
    *   **Type**: String (JSON)
    *   **TTL**: 1 hour
    *   **Purpose**: Cache individual register details to reduce DB load on frequent access.

### Real-time Statistics
*   `registers:ui:stats`
    *   **Type**: Hash
    *   **Fields**:
        *   `PENDING`: Count of pending registers
        *   `ANALYZED`: Count of analyzed registers
        *   `TOTAL`: Total count
    *   **Update Trigger**: Updated atomically via Lua script or service logic when events are processed.

### Session/User
*   `users:session:{token}`
    *   **Type**: String
    *   **TTL**: 24 hours
    *   **Purpose**: Store active user session data.

## Expiration Policy
*   Cache keys should always have a TTL to prevent memory leaks.
*   Stats keys are persistent but should be recalculated/verified periodically (e.g., nightly job).
