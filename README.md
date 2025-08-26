# Inventory Service

**Inventory Service** is a microservice responsible for managing product stock and availability within the **MicroServiceGrid** ecosystem.  
It ensures that orders can only be placed when sufficient items are available in stock.

---

## 🚀 Features
- Track stock levels for products (by SKU)
- Validate product availability during order creation
- Reserve and release stock for ongoing orders
- Update inventory after order completion or cancellation
- Expose REST API for other services (e.g., Order Service)

---

## 🛠️ Tech Stack
- **Java 17**
- **Spring Boot 3**
- **Spring Data JPA** with PostgreSQL (or MongoDB)
- **Apache Kafka** for asynchronous stock events
- **Docker & Kubernetes** for containerization and orchestration

---

👨‍💻 **Author**: Andrij72 — demo project exploring microservice architecture with Spring Boot, Kafka, and Kubernetes.
