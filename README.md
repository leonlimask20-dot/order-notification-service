# Order Notification Service

[![CI](https://github.com/leonlimask20-dot/order-notification-service/actions/workflows/ci.yml/badge.svg)](https://github.com/leonlimask20-dot/order-notification-service/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.9-black?logo=apachekafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

Event-driven notification service built with Java 17 + Spring Boot 3.5 and
**Apache Kafka**. It consumes events published by `order-processing-api`,
processes them and persists notifications in PostgreSQL, with **Dead Letter
Queue** support for failure handling.

## Quick links

| | |
|---|---|
| Simulate order placed | `POST http://localhost:8080/api/v1/simulate/order-placed` |
| Simulate order cancelled | `POST http://localhost:8080/api/v1/simulate/order-cancelled` |
| List notifications | `GET http://localhost:8080/api/v1/notifications` |
| Notifications by order | `GET http://localhost:8080/api/v1/notifications/order/{orderId}` |
| Run with Docker | [Go to section](#running) |

## Key skills demonstrated

- Event-driven architecture with Apache Kafka (producer and consumer)
- KRaft mode — Kafka without ZooKeeper (default since Kafka 3.3)
- Dead Letter Queue (DLQ): messages that fail 3 times are redirected to a separate topic
- Consumer groups for distributed processing
- Clean Architecture applied: domain isolated from Kafka, JPA and Spring
- Persisting events as notifications with Spring Data JPA and PostgreSQL
- Automatic retry with fixed backoff via `DefaultErrorHandler`
- Containerization with Docker Compose (Kafka + PostgreSQL)

## Tech stack

| Technology | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.13 |
| Spring Kafka | 3.3 |
| Apache Kafka | 3.9 (KRaft) |
| Spring Data JPA | 3.x |
| PostgreSQL | 16 |
| Docker + Docker Compose | — |

## Event flow

```
1. POST /simulate/order-placed     →  OrderEventProducer
2. OrderEventProducer              →  publishes OrderPlacedEvent to the order-placed-events topic
3. OrderEventConsumer              →  @KafkaListener consumes the event
4. ProcessOrderEventUseCase        →  creates a Notification, persists it with status SENT
5. NotificationRepositoryImpl      →  saves it via JPA to PostgreSQL
6. GET /notifications              →  returns the saved notifications
```

If the consumer fails, `DefaultErrorHandler` retries 3 times with a 2-second
interval. After the third error, the message is published to the `.DLT` topic
and `DLQConsumer` receives it for handling.

## Architecture

```
src/main/java/com/lnl/notification/
├── domain/                        → Core. Zero external dependencies.
│   ├── entity/                    │   Notification
│   ├── enums/                     │   NotificationType, NotificationStatus
│   └── repository/                │   NotificationRepository (interface — outbound port)
│
├── application/                   → Use cases. Orchestrates the domain.
│   └── usecase/                   │   ProcessOrderEventUseCase (plain POJO, no @Service)
│
├── adapters/                      → Translators between domain and the outside world
│   ├── kafka/
│   │   ├── consumer/              │   OrderEventConsumer, DLQConsumer
│   │   ├── producer/              │   OrderEventProducer (simulates order-processing-api)
│   │   └── event/                 │   OrderPlacedEvent, OrderCancelledEvent (Kafka DTOs)
│   ├── persistence/               │   NotificationEntity, NotificationRepositoryImpl
│   └── controller/                │   NotificationController, SimulateOrderRequest
│
└── infrastructure/                → Configuration and error handling
    ├── config/                    │   KafkaConfig (topics, DLQ, error handler), BeanConfig
    └── exception/                 │   GlobalExceptionHandler
```

## Kafka topics

| Topic | Type | Description |
|---|---|---|
| `order-placed-events` | Main | Order placed events |
| `order-placed-events.DLT` | Dead Letter | Messages that failed 3 times |
| `order-cancelled-events` | Main | Order cancelled events |
| `order-cancelled-events.DLT` | Dead Letter | Messages that failed 3 times |

Topics are created automatically by the application via `TopicBuilder` on startup.

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker Desktop

## Running

```bash
# Start Kafka (KRaft) + PostgreSQL
docker-compose up -d

# Start the application
mvn spring-boot:run
```

API available at `http://localhost:8080`.

> **Note:** PostgreSQL uses port 5434 to avoid conflicts with other local instances.

## Endpoints

### Event simulation

| Method | Route | Description |
|---|---|---|
| `POST` | `/api/v1/simulate/order-placed` | Publishes an order-placed event to Kafka |
| `POST` | `/api/v1/simulate/order-cancelled` | Publishes an order-cancelled event to Kafka |

### Notifications

| Method | Route | Description |
|---|---|---|
| `GET` | `/api/v1/notifications` | List all notifications |
| `GET` | `/api/v1/notifications/order/{orderId}` | Notifications for a specific order |

## Examples

### Simulate an order placed

```bash
curl -X POST http://localhost:8080/api/v1/simulate/order-placed \
  -H "Content-Type: application/json" \
  -d '{"orderId": "ord-001", "customerId": "customer-001"}'
```

```
ORDER_PLACED event published: ord-001
```

### Query an order's notifications

```bash
curl http://localhost:8080/api/v1/notifications/order/ord-001
```

```json
[
  {
    "id": "6c575c36-32f6-4f59-95f1-d398a346eadb",
    "orderId": "ord-001",
    "customerId": "customer-001",
    "type": "ORDER_PLACED",
    "message": "Your order ord-001 has been received and is being processed.",
    "status": "SENT",
    "createdAt": "2026-04-11T19:22:46"
  },
  {
    "id": "c46e4269-28e7-4658-ba02-dc1a05191b99",
    "orderId": "ord-001",
    "customerId": "customer-001",
    "type": "ORDER_CANCELLED",
    "message": "Your order ord-001 has been cancelled.",
    "status": "SENT",
    "createdAt": "2026-04-11T19:23:34"
  }
]
```

## 🤖 Agent Architecture

This project was built and code-reviewed using a **multi-agent
context-optimization workflow**: specialized AI agents each audit a single
architectural layer — domain, use cases, adapters, infrastructure, tests —
within a strict context budget. The approach cuts review time and token cost
while keeping full traceability of every finding.

Methodology, agent templates and the full playbook: **[leonlim3.gumroad.com](https://leonlim3.gumroad.com)**

## Production considerations

- Replace `ddl-auto=update` with migrations using Flyway or Liquibase
- Add a Schema Registry (Confluent) for event versioning with Avro
- Implement consumer idempotency to avoid duplicate processing
- Monitor consumer group lag with Kafka UI or Prometheus + Grafana
- Move sensitive configuration to environment variables

## Author

LNL &nbsp; GitHub: [@leonlimask20-dot](https://github.com/leonlimask20-dot) &nbsp; Email: leonlimask@gmail.com
