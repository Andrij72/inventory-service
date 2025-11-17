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

## 🔗 Service Integration

The **Inventory Service** communicates with the **Order Service** via a **Feign Client**.  
All inter-service communication is routed through the **API Gateway**, which handles:
- Circuit breaking
- Rate limiting
- Retries and fallback logic (powered by Resilience4j)

This design ensures system resilience and prevents cascading failures between services.

---

## ⚙️ Environment Configuration

All sensitive parameters (like database credentials) are stored in the `.env` file located in the **project root directory**.

### 📄 Example `.env` file
ROOT_MYSQL_PASS=your_root_password
MYSQL_USER=inventory_user
MYSQL_PASS=inventory_password

These variables are used inside the `docker-compose.*yml`:

```yaml
environment:
  ROOT_MYSQL_PASS: ${ROOT_MYSQL_PASS}
  MYSQL_USER: ${MYSQL_USER}
  MYSQL_PASSWORD: ${MYSQL_PASS}  
```
---
## 🐳 Docker Compose Examples
I provided multiple Docker Compose configurations to simplify local development, intermediate dev builds,
and production deployment:

    docker-compose-examples/
    ├── docker-compose.local.yml     # Starts only MySQL; Inventory Service must be run manually via IntelliJ or ./mvnw spring-boot:run
    ├── docker-compose.override.yml  # Starts MySQL + Inventory Service image (dev-latest) for intermediate development
    ├── docker-compose.prod.yml      # Starts MySQL + Inventory Service release image for productionigurations to simplify local development, intermediate dev builds, and production deployment:


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

Current implementation supports the following endpoint:

| Method | Endpoint                         | Description                          |
|--------|----------------------------------|--------------------------------------|
| GET    | `/api/v1/inventory`              | Check if a product is available in stock |

**Example request:**
GET /api/v1/inventory?sku=ABC123&quantity=5

**Response:**
true

Additional endpoints for stock reservation, release, and update will be added in future releases.

------
## 🛠️ Development Workflow

CI/CD via GitHub Actions:

- `develop` branch → builds **dev-latest** Docker image
- `main` branch → builds **latest** Docker image
- Tags (e.g., `v0.0.1`) → build release image
- Tests run via Maven/Gradle; optionally connect to MySQL via ENV variable for integration tests
- Docker images use SHA tags for reproducibility

---

## 🧪 Integration Tests

The project includes both lightweight unit tests and full integration tests:

- ✅ Light tests: run via MockMvc to validate REST endpoints and business logic without starting the full context.

- ✅ Full integration tests: use Testcontainers to start a real MySQL instance and verify end-to-end functionality (database + HTTP layer).

These tests ensure the service is stable, isolated, and ready for production-like environments.

---

## 👨‍💻 Author

Andrij72 — demo project exploring microservice architecture with Spring Boot, Kafka, and Kubernetes