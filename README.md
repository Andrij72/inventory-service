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
- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA** with PostgreSQL (or MySQL)
- **Apache Kafka** for asynchronous stock events
- **Docker & Kubernetes** for containerization and orchestration

---

## 📂 Project Structure
    inventory-service/
    ├── src/main/java/com/akul/microservices/inventory
    │ ├── controller # REST controllers
    │ ├── model # Data models
    │ ├── repository # JPA repositories
    │ └── service # Business logic
    ├── src/main/resources
    │ ├── db/migration # Flyway SQL scripts
    │ ├── application.properties
    │ └── application-docker.properties
    ├── docker-compose-examples/
    │ ├── docker-compose.local.yml
    │ ├── docker-compose.override.yml
    │ └── docker-compose.prod.yml
    ├── Dockerfile
    └── README.md

---
## 🐳 Docker Compose Examples

I provided multiple Docker Compose configurations to simplify local development, intermediate dev builds,
and production deployment:

    docker-compose-examples/
    ├── docker-compose.local.yml # Starts only MySQL; Inventory Service must be run manually via IntelliJ or ./mvnw spring-boot:run
    ├── docker-compose.override.yml # Starts MySQL + Inventory Service image (dev-latest) for intermediate development
    ├── docker-compose.prod.yml # Starts MySQL + Inventory Service release image for productionigurations to simplify local development, intermediate dev builds, and production deployment:


- **Local development**: MySQL only; run Inventory Service via IntelliJ or CLI.
- **Override / dev build**: MySQL + service dev image (`develop` / `dev-latest`) for testing features quickly.
- **Production**: MySQL + release image (`vX.X.X`) ready for deployment.

---

## ⚙️ Running Locally

1️⃣ Clone the repository:
```bash
git clone https://github.com/Andrij72/inventory-service.git
```
```bash
docker-compose -f docker-compose-examples/docker-compose.local.yml up -d
```
2️⃣ Start dependencies with Docker Compose (local MySQL only):
```bash
docker-compose -f docker-compose-examples/docker-compose.local.yml up -d
```
3️⃣ Start the Inventory Service manually:
```bash
./mvnw spring-boot:run
```
or
```bash
./gradlew bootRun
```

-----
## 📌 REST API Endpoints
| Method | Endpoint               | Description                     |
|--------|-----------------------|---------------------------------|
| GET    | /api/inventory        | Get all inventory items         |
| GET    | /api/inventory/{id}   | Get a specific inventory item  |
| POST   | /api/inventory        | Create a new inventory record  |
| PUT    | /api/inventory/{id}   | Update an inventory record     |
| DELETE | /api/inventory/{id}   | Delete an inventory record     |

------
## 🛠️ Development Workflow

CI/CD via GitHub Actions:

- `develop` branch → builds **dev-latest** Docker image
- `main` branch → builds **latest** Docker image
- Tags (e.g., `v0.0.1`) → build release image
- Tests run via Maven/Gradle; optionally connect to MySQL via ENV variable for integration tests
- Docker images use SHA tags for reproducibility

---

## 📌 Roadmap

- Add request validation (Spring Validation)
- Add JUnit + Testcontainers tests
- Integrate with Order Service using **Resilience + Feign**
- Add service monitoring with Prometheus + Grafana

---

## 👨‍💻 Author

Andrij72 — demo project exploring microservice architecture with Spring Boot, Kafka, and Kubernetes