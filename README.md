# 🚀 Distributed Feature Flag Platform

## 📌 Overview

The **Distributed Feature Flag Platform** is a cloud-native microservices application built with Spring Boot that allows teams to manage and evaluate feature flags in a distributed environment.

This project was designed to demonstrate senior-level engineering skills in:

- Microservices architecture
- Spring ecosystem (Boot, Security, MVC)
- Event-driven systems
- Cloud-native development
- DevOps and containerization
- Observability and resilience patterns

The platform enables real-time feature management, secure access control, and scalable evaluation of feature rules.

---

## 🏗 Architecture

The system follows a **microservices architecture** where each service has a single responsibility and its own database.

### Core Services

| Service | Description |
|--------|-------------|
| **API Gateway** | Central entry point, routing and security filtering |
| **Auth Service** | Authentication, JWT generation and RBAC |
| **Flag Service** | Feature flag management and configuration |
| **Evaluation Service** | High-performance flag evaluation with caching |
| **Event Service** | Asynchronous communication via Kafka |

Each service is independently deployable and containerized.

---

## 🛠 Technologies

### Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Spring Cloud Gateway
- Resilience4j

### Messaging

- Apache Kafka

### Databases

- PostgreSQL
- Redis

### DevOps & Infrastructure

- Docker
- Docker Compose
- Kubernetes (planned)
- GitHub Actions (CI/CD)

### Observability

- Spring Boot Actuator
- Micrometer
- Prometheus & Grafana (planned)

---

## 🔐 Security

- JWT-based authentication
- Role-based access control (ADMIN, DEVELOPER)
- Secure password encryption (BCrypt)
- Gateway-level token validation

---

## 🧪 Testing Strategy

- Unit tests (JUnit 5 + Mockito)
- Integration tests
- Testcontainers (for database and Kafka integration)
- API documentation via OpenAPI

---

## 📂 Project Structure

```
distributed-feature-flags/
│
├── gateway/
├── auth-service/
├── flag-service/
├── evaluation-service/
├── event-service/
│
├── docker-compose.yml
└── README.md
```

---

## 🚦 Running Locally (Initial Setup)

**Build the services:**

```bash
mvn clean install
```

**Start infrastructure and services:**

```bash
docker-compose up --build
```

**Access services:**

- **Auth Service:** http://localhost:8081
- **Gateway:** http://localhost:8080

*(Ports may vary depending on configuration.)*

---

## 🎯 Project Goals

- Demonstrate solid software engineering fundamentals
- Apply clean architecture and SOLID principles
- Implement secure and scalable microservices
- Apply DevOps best practices
- Showcase production-ready system design
