# Lutaco Backend API

Lutaco is a modern, production-ready backend service built with Spring Boot.
It provides a secure RESTful API for authentication, transaction management, data processing, background jobs, and reporting/export features.

The system follows a layered architecture and is designed for scalability, maintainability, and secure data handling.

---

## 🚀 Technology Stack

### Core Platform

* Java 17
* Spring Boot 3.5.x

### Data & Persistence

* Spring Data JPA
* MySQL
* Liquibase (database schema versioning)

### Web Layer

* Spring Web (REST APIs)
* Spring WebFlux (reactive APIs where needed)

### Security

* Spring Security
* OAuth2 Authorization Server
* JWT-based authentication & authorization

### API Documentation

* springdoc-openapi
* Swagger UI available at:

```
/swagger-ui/index.html
```

### Utilities & Libraries

* Lombok – reduce boilerplate code
* MapStruct – DTO/entity mapping
* Apache POI – Excel import/export
* Caffeine – in-memory caching

### Build & Deployment

* Maven – build tool
* Docker + docker-compose – containerized deployment
* GitHub Actions – CI/CD pipeline

---

## 🏗️ Project Architecture

The project follows a clean layered architecture:

```
controller  → REST endpoints
service     → business logic
repository  → data access layer (JPA)
entity      → database models
security    → authentication & authorization
jwt         → token generation & validation
export      → Excel export functionality
job         → scheduled background tasks
insight     → analytics / monitoring / reporting
```

This structure ensures separation of concerns and makes the system easier to test and maintain.

---

## 🔐 Core Features

### Authentication & Authorization

* Secure login & registration
* JWT access/refresh token flow
* Role-based access control
* OAuth2 compatible token issuing

### Transaction Management

* Create/update/delete financial transactions
* Bulk operations support
* Ownership validation per user
* History tracking

### Wallet Management

* User wallets and balances
* Status management (active/inactive)
* Secure ownership enforcement

### Recurring Transactions

* Scheduled recurring payments
* Automatic execution jobs
* Status lifecycle handling

### Reporting & Insights

* Dashboard statistics
* Expense summaries
* Data aggregation logic

### Data Export

* Export reports to Excel files
* Powered by Apache POI

### Background Jobs

* Scheduled tasks execution
* Data cleanup / automation logic

### Error Handling

* Global exception handling
* Consistent error response structure
* Detailed logging for debugging

---

## 📄 API Documentation

Swagger UI is available after running the application:

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON spec:

```
/v3/api-docs
```

---

## 🐳 Running with Docker

Start the system with:

```bash
docker-compose up --build
```

The application will be available at:

```
http://localhost:8080 (default)
```

---

## ⚙️ Local Development

### Prerequisites

* Java 17+
* Maven
* MySQL running locally or via Docker

### Run application

```bash
mvn spring-boot:run
```

---

## 🧪 Testing Strategy

The project uses a layered testing approach:

### Unit Tests

* Service layer business logic
* Controller validation logic
* Security edge cases

### Integration Tests

* Database interactions
* API flow validation

### Manual / API Testing

* Swagger UI
* Postman collections

---

## 📦 CI/CD

GitHub Actions workflows are configured to:

* build the project
* run tests
* validate Docker image builds

---

## 📜 License

This project is proprietary and intended for internal or authorized use only.
