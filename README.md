# 🧾 Inventory Service

**Inventory Service** is a microservice responsible for managing product stock and availability within the **MicroServiceGrid** ecosystem  
([MicroserviceGrid-DDD](https://github.com/Andrij72/MicroserviceGrid-DDD)).

It ensures that orders can only be placed when sufficient items are available in stock and handles reservations using the **Saga + Outbox pattern** with TTL support.

---

## 📌 Versioning

This implementation represents the **Saga-based evolution** of the Inventory Service.

The previous **CRUD (legacy) version** is still available in the `develop_crud` branch:

👉 [Legacy Inventory Service README](https://github.com/Andrij72/inventory-service/blob/develop_crud/README.md)

---

## 🚀 Features

- Track stock levels for products (by SKU)
- Validate product availability during order creation
- Reserve and release stock for ongoing orders
- TTL-based automatic expiration of reservations
- Multi-SKU order handling
- Event-driven architecture using Kafka + Outbox pattern
- Integration with Avro Schema Registry
- Idempotent reservation handling
- REST API for internal and external services

---

## 🛠️ Tech Stack

- Java 21, Spring Boot 3
- Spring Data JPA (PostgreSQL)
- Apache Kafka
- Apache Avro (via microservices-schema-registry)
- Docker & Docker Compose
- Testcontainers
- JUnit 5 + MockMvc

---

## 📂 Project Structure

```text
    inventory-service/
    ├── application
    │ ├── dto
    │ ├── mapper
    │ └── service
    │ ├── InventoryReservationService
    │ ├── InventoryOutboxSagaHandler
    │ └── AdminInventoryService
    ├── domain/model
    │ ├── Inventory
    │ ├── InventoryReservation
    │ └── InventoryEvent
    ├── infrastructure
    │ ├── messaging # Kafka listeners
    │ ├── outbox # Outbox entity + repo
    │ ├── persistence # JPA repositories
    │ └── worker # Outbox publisher
    ├── web/controller
    │ ├── InventoryController
    │ └── AdminInventoryController
```

---

## 🔗 Service Integration

**Order Service → Inventory Service**

1. OrderService sends `OrderPlacedEvent` via Kafka
2. Inventory Service checks stock, reserves items, saves events to Outbox
3. Outbox Worker publishes events back to Kafka
4. OrderService continues or cancels order based on response

All events use Avro schemas from `microservices-schema-registry`

---

## ⚙️ Configuration

```properties
inventory.reservation.minutes=5
inventory.ttl.check.millis=60000
```
* Reservation expires after 5 minutes
* TTL cleanup runs every 60 seconds

---

### Public API (InventoryController)

| Method		 | Endpoint                  | Description          |
|----------|---------------------------|---------------------------|
| GET	    | /api/v1/inventory/check	| Check stock availability  |
| POST	    | /api/v1/inventory/reserve | Reserve inventory         |
| POST     | /api/v1/inventory/confirm | Confirm reservation       |
| POST     | /api/v1/inventory/cancel  | Cancel reservation     <br/>   |

*Example:*
```http
POST /api/v1/inventory/reserve
Content-Type: application/json
```
```json
{
  "orderId": "ORDER-1",
  "items": [
    { "skuCode": "S24-ULTRA", "quantity": 2 }
  ]
}
```

### Admin API (AdminInventoryController)

| Method  | Endpoint                      | Description     |
|---------|-------------------------------|-----------------|
| POST    | /api/v1/admin/inventory       | Create item     |
| GET     | /api/v1/admin/inventory       | Get all items   |
| GET     | /api/v1/admin/inventory/{sku} | Get item by SKU |
| PUT     | /api/v1/admin/inventory/{sku} | Update item     |
| DELETE  | /api/v1/admin/inventory/{sku} | Delete item     |
| GET     | /api/v1/admin/inventory/check | Check stock   <br/>  |
		
---		
		
## 🔄 Saga Flow (Outbox Pattern)

```textmate
     Order Service
          │
          │ OrderPlacedEvent
          ▼
     Inventory Service
          │
          ├── check stock
          ├── reserve inventory
          ├── create reservation (TTL)
          ├── save event to Outbox
          │
          ▼
     Outbox Worker → Kafka
          │
          ▼
     Order Service
          ├── CONFIRMED → continue
          └── REJECTED → cancel
                 
```

### ⏳ TTL Expiration Flow


```text
     Scheduler
          │
          ├── find expired reservations
          ├── mark EXPIRED
          ├── release inventory
          ├── publish INVENTORY_EXPIRED
          ▼
Kafka → Order Service
                
```
---
## 🧠 Key Design Decisions

* TTL reservations prevent stock locking
* Idempotent operations via unique (order_id, sku_code)
* Outbox Pattern ensures reliable event delivery
* Multi-SKU orders are processed independently
---

### 🧠 Flow Explanation

This flow represents the interaction between Order and Inventory services using the **Saga + Outbox pattern**.

- Order Service publishes `OrderPlacedEvent`
- Inventory Service reserves stock and stores the result in the **Outbox**
- Outbox Worker guarantees reliable delivery to Kafka
- Order Service reacts to `CONFIRMED` or `REJECTED` events and updates order status

### ⏳ TTL Behavior

Inventory reservations are time-limited:

- If not confirmed in time, they expire automatically
- Scheduler releases reserved stock
- `INVENTORY_EXPIRED` event is published
- Order Service marks the order as **FAILED**

This prevents stock from being locked indefinitely and ensures system consistency.

---
## 🧪 Tests

Integration tests using Testcontainers covering:

1. [ ]  INVENTORY_CONFIRMED
2. [ ]  INVENTORY_REJECTED
3. [ ]  TTL expiration
4. [ ]  Multi-SKU flow
---

## 🐳 Running Locally

```bash
git clone https://github.com/Andrij72/inventory-service.git
cd inventory-service
``` 
Start infrastructure:
```bash
docker-compose -f docker-compose.local.yml up -d
```
Run service:
```bash
./mvnw spring-boot:run
```

---
## 📌 Testing Endpoints

You can test the Order Service endpoints using Postman.  

Import the Postman collection from the project root:
```
.\MicroserviceGrid.posman_inventory_reservation.json
.\MicroServiceGrid_AdminInventoryCollection.json
```
---

## ⚙️ CI/CD

* feature/* → run tests
* develop → dev-latest Docker image
* main → latest Docker image
* vX.X.X → release image

---
## 📦 Why Outbox Pattern?

The Outbox pattern is used to guarantee reliable event publishing:

- Prevents message loss between DB transaction and Kafka publish
- Ensures events are stored before being sent
- Enables retry via Outbox Worker
- Provides eventual consistency without distributed transactions
---

## ⚠️ Failure Handling

- If inventory is insufficient → `INVENTORY_REJECTED`
- If reservation expires → `INVENTORY_EXPIRED`
- Order Service reacts and marks order as **FAILED**

No direct rollback calls are used — only events.

---

## 👨‍💻 Author

_Andrij72_ — Microservices, Kafka, Saga, Outbox, Avro, Spring Boot

---