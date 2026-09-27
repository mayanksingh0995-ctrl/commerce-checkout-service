# Checkout Service

Owns checkout state and coordinates the checkout saga. Public clients use the REST API; internal inventory calls use gRPC and the versioned commerce contracts. Checkout persists command state, a query projection, and a transactional outbox in its own PostgreSQL database, then publishes events to Kafka.

## Requirements

- JDK 21 and Maven 3.9+
- PostgreSQL 17 or compatible and Kafka
- Published `com.acme.commerce:contracts:1.0.0` package from the `commerce-contracts` repository
- Running Inventory Service at `localhost:9091` by default

Configure the `commerce-contracts` GitHub Packages credentials in Maven `settings.xml`, then set `-Dgithub.owner=mayanksingh0995-ctrl` when building.

```powershell
mvn -Dgithub.packages=true -Dgithub.owner=mayanksingh0995-ctrl clean verify
mvn -Dgithub.packages=true -Dgithub.owner=mayanksingh0995-ctrl spring-boot:run
```

Checkout HTTP defaults to `8080`; PostgreSQL defaults to `localhost:5434`; Kafka defaults to `localhost:9092`. Configure with `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `INVENTORY_GRPC_ADDRESS`, and `KAFKA_BOOTSTRAP_SERVERS`.

Create a checkout with a client-generated UUID as an idempotency key:

```json
{
  "checkoutId": "550e8400-e29b-41d4-a716-446655440000",
  "sku": "DEMO-SKU-001",
  "quantity": 2
}
```

Submit it to `POST /api/checkouts`; query the projection with `GET /api/checkouts/{checkoutId}`. Event delivery is at-least-once; consumers must deduplicate by event ID.