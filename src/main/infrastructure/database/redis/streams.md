# Redis Streams Strategy

This document outlines the Redis Streams architecture used for handling event sourcing in the Prevenção de Perdas system.

## Stream: `registers:events`

All state changes to `Register` entities are published to this stream.

### Event Structure

Events are stored as JSON strings in a field named `payload`.

```json
{
  "eventId": "uuid",
  "aggregateId": "register_id",
  "type": "REGISTER_CREATED | REGISTER_UPDATED | REGISTER_ANALYZED",
  "timestamp": "ISO-8601",
  "data": {
    // Full entity state or delta
    "id": 123,
    "protocol": "abc-123",
    "status": "PENDING",
    ...
  },
  "metadata": {
    "userId": 456,
    "source": "api"
  }
}
```

### Consumer Groups

1.  **`inventory-group`**
    *   **Purpose**: Updates the read model or triggers inventory adjustments in external systems.
    *   **Consumers**: `inventory-service-1`, `inventory-service-2`

2.  **`analytics-group`**
    *   **Purpose**: Aggregates data for dashboards and reports.
    *   **Consumers**: `analytics-service`

## commands

*   **Add Event**: `XADD registers:events * payload '{json...'`
*   **Read Group**: `XREADGROUP GROUP inventory-group consumer1 COUNT 10 BLOCK 2000 STREAMS registers:events >`
