# E-Commerce Microservices

A Spring Boot + Spring Cloud microservices project demonstrating service discovery, inter-service communication via OpenFeign, API gateway routing, and fault tolerance with Resilience4j.

## Architecture

```
                         ┌──────────────────┐
                         │  API Gateway      │
                         │  (port 8080)      │
                         └────────┬─────────┘
                                  │  /api/v1/{service}/**
                ┌─────────────────┼─────────────────┐
                ▼                 ▼                  ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐
        │  Order       │ │  Inventory   │ │  Shipping        │
        │  Service     │ │  Service     │ │  Service         │
        │  (9020)      │ │  (9010)      │ │  (9030)          │
        └──────┬───────┘ └──────────────┘ └──────────────────┘
               │               ▲
               │  OpenFeign    │
               └───────────────┘
               │  OpenFeign    │
               └───────────────▶ (Shipping Service)

                         ┌──────────────────┐
                         │  Eureka Server   │
                         │  (8761)          │
                         └──────────────────┘
```

## Tech Stack

| Component | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Cloud | 2025.1.3 |
| PostgreSQL | Any recent |
| Resilience4j | Via Spring Cloud CircuitBreaker |

Key libraries: Spring Cloud OpenFeign, Netflix Eureka Client, Spring Cloud Gateway, Lombok, ModelMapper.

## Prerequisites

- Java 21 installed and on PATH
- Maven installed (or use the included `mvnw` wrapper)
- PostgreSQL running locally with three databases created

```sql
CREATE DATABASE inventoryDB;
CREATE DATABASE orderDB;
CREATE DATABASE shippingDB;
```

## Getting Started

**Start order matters** — Eureka first, then services:

```
1.  discovery-service      → http://localhost:8761
2.  inventory-service      → port 9010
3.  order-service          → port 9020
4.  shipping-service       → port 9030
5.  api-gateway            → port 8080 (default)
```

All services register with Eureka automatically. The database tables are created on startup (`ddl-auto=create-drop`), and inventory DB is seeded with product data via `data.sql`.

## Services & Endpoints

### Discovery Service (`:8761`)

Eureka dashboard at http://localhost:8761. Shows all registered services.

### API Gateway (`:8080`)

All client requests go through the gateway:

| Route | Service |
|---|---|
| `/api/v1/orders/**` | ORDER-SERVICE |
| `/api/v1/inventory/**` | INVENTORY-SERVICE |
| `/api/v1/shipping/**` | SHIPPING-SERVICE |

### Inventory Service (`:9010`, context-path `/inventory`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/inventory/products` | List all products |
| `GET` | `/inventory/products/{id}` | Get product by ID |
| `PUT` | `/inventory/products/reduce-stocks` | Reduce stock (called by order-service) |
| `PUT` | `/inventory/products/add-stocks` | Restock items (called by order-service on cancel) |

### Order Service (`:9020`, context-path `/orders`)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/orders/core/create-order` | Place an order |
| `GET` | `/orders/core` | List all orders |
| `GET` | `/orders/core/{id}` | Get order by ID |
| `PUT` | `/orders/core/cancel-order/{id}` | Cancel order and restock |
| `GET` | `/orders/core/shipment-status/{orderId}` | Confirm shipping status |

### Shipping Service (`:9030`, context-path `/shipping`)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/shipping/core/create-shipment/{orderId}` | Create a shipment |
| `PUT` | `/shipping/core/update-shipment` | Advance shipment status |
| `GET` | `/shipping/core/{orderId}` | Get shipment by order ID |

## Testing the Full Flow

```bash
# 1. Create an order (reduce stocks, save CONFIRMED)
curl -X POST http://localhost:8080/api/v1/orders/core/create-order \
  -H "Content-Type: application/json" \
  -d '{"items": [{"productId": 1, "quantity": 2}]}'

# 2. Create a shipment for the order (uses the orderId from step 1)
curl -X POST http://localhost:8080/api/v1/shipping/core/create-shipment/1

# 3. Advance shipment to SHIPPED
curl -X PUT http://localhost:8080/api/v1/shipping/core/update-shipment \
  -H "Content-Type: application/json" \
  -d '{"orderId": 1, "shipmentStatus": "SHIPPED"}'

# 4. Confirm shipping status from order-service (Feign call to shipping-service)
curl http://localhost:8080/api/v1/orders/core/shipment-status/1

# 5. Test the circuit breaker fallback — stop shipping-service, then call again
#    order-service retries 3x, then returns an empty DTO instead of a 500

# 6. Cancel the order (restocks inventory)
curl -X PUT http://localhost:8080/api/v1/orders/core/cancel-order/1
```

## Resilience Patterns

### Order Placement — Circuit Breaker

`createOrder` in order-service is protected by a circuit breaker (`inventoryCircuitBreaker`). If inventory-service is down, a fallback method returns an empty DTO instead of propagating the error.

### Shipping Confirmation — Retry + Circuit Breaker

`getShipmentStatus` in order-service applies both mechanisms:

1. **Retry** (`shippingRetry`): up to 3 attempts, 100ms wait between each.
2. **Circuit Breaker** (`shippingCircuitBreaker`): opens after 50% of calls fail (minimum 10 calls), stays open for 1s, then allows 3 test calls in half-open state.
3. **Fallback**: if both retry and breaker fail, returns an empty `ShipmentRecordDto` with `null` status.

### Cancel Order

No remote resilience needed — cancelOrder makes a single Feign call to inventory (add stocks) and sets status to CANCELLED locally. Duplicate cancellations are blocked by a status guard (only PENDING/CONFIRMED cancellable).

## Shipment Status Lifecycle

```
PENDING → IN_TRANSIT → SHIPPED → DELIVERED
  │          │           │
  └──────────┴── CANCELLED (terminal states locked)
```

Invalid transitions and null status values are rejected with a clear error message.

## Future Enhancements

- **Apache Kafka** for event-driven inter-service communication (order placed → shipment created automatically)
- **PENDING order status** with Saga pattern for distributed transaction management
- **Docker Compose** for containerized deployment
- **Centralized configuration** with Spring Cloud Config
