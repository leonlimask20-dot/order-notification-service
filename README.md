# Order Notification Service

[![CI](https://github.com/leonlimask20-dot/order-notification-service/actions/workflows/ci.yml/badge.svg)](https://github.com/leonlimask20-dot/order-notification-service/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.9-black?logo=apachekafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

Serviço de notificações orientado a eventos construído com Java 17 + Spring Boot 3.5 e **Apache Kafka**. Consome eventos publicados pelo `order-processing-api`, processa e persiste notificações no PostgreSQL com suporte a **Dead Letter Queue** para tratamento de falhas.

## Links rápidos

| | |
|---|---|
| Simular pedido criado | `POST http://localhost:8080/api/v1/simulate/order-placed` |
| Simular pedido cancelado | `POST http://localhost:8080/api/v1/simulate/order-cancelled` |
| Listar notificações | `GET http://localhost:8080/api/v1/notifications` |
| Notificações por pedido | `GET http://localhost:8080/api/v1/notifications/order/{orderId}` |
| Rodar com Docker | [Ir para seção](#execução) |

## Principais competências demonstradas

- Arquitetura orientada a eventos com Apache Kafka (producer e consumer)
- KRaft mode — Kafka sem ZooKeeper (padrão desde Kafka 3.3)
- Dead Letter Queue (DLQ): mensagens que falham 3x são redirecionadas para tópico separado
- Consumer groups para processamento distribuído
- Clean Architecture aplicada: domínio isolado de Kafka, JPA e Spring
- Persistência de eventos como notificações com Spring Data JPA e PostgreSQL
- Retry automático com backoff fixo via `DefaultErrorHandler`
- Containerização com Docker Compose (Kafka + PostgreSQL)

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.13 |
| Spring Kafka | 3.3 |
| Apache Kafka | 3.9 (KRaft) |
| Spring Data JPA | 3.x |
| PostgreSQL | 16 |
| Docker + Docker Compose | — |

## Fluxo de um evento

```
1. POST /simulate/order-placed     →  OrderEventProducer
2. OrderEventProducer              →  publica OrderPlacedEvent no tópico order-placed-events
3. OrderEventConsumer              →  @KafkaListener consome o evento
4. ProcessOrderEventUseCase        →  cria Notification, persiste com status SENT
5. NotificationRepositoryImpl      →  salva via JPA no PostgreSQL
6. GET /notifications              →  retorna notificações salvas
```

Em caso de falha no consumer, o `DefaultErrorHandler` tenta 3x com intervalo de 2 segundos. Após o terceiro erro, a mensagem é publicada no tópico `.DLT` e o `DLQConsumer` a recebe para tratamento.

## Arquitetura

```
src/main/java/com/lnl/notification/
├── domain/                        → Núcleo. Zero dependências externas.
│   ├── entity/                    │   Notification
│   ├── enums/                     │   NotificationType, NotificationStatus
│   └── repository/                │   NotificationRepository (interface — porta de saída)
│
├── application/                   → Casos de uso. Orquestra o domínio.
│   └── usecase/                   │   ProcessOrderEventUseCase (POJO puro, sem @Service)
│
├── adapters/                      → Tradutores entre domínio e mundo externo
│   ├── kafka/
│   │   ├── consumer/              │   OrderEventConsumer, DLQConsumer
│   │   ├── producer/              │   OrderEventProducer (simula o order-processing-api)
│   │   └── event/                 │   OrderPlacedEvent, OrderCancelledEvent (DTOs Kafka)
│   ├── persistence/               │   NotificationEntity, NotificationRepositoryImpl
│   └── controller/                │   NotificationController, SimulateOrderRequest
│
└── infrastructure/                → Configuração e tratamento de erros
    ├── config/                    │   KafkaConfig (tópicos, DLQ, error handler), BeanConfig
    └── exception/                 │   GlobalExceptionHandler
```

## Tópicos Kafka

| Tópico | Tipo | Descrição |
|---|---|---|
| `order-placed-events` | Principal | Eventos de pedido criado |
| `order-placed-events.DLT` | Dead Letter | Mensagens que falharam 3x |
| `order-cancelled-events` | Principal | Eventos de pedido cancelado |
| `order-cancelled-events.DLT` | Dead Letter | Mensagens que falharam 3x |

Os tópicos são criados automaticamente pela aplicação via `TopicBuilder` ao subir.

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker Desktop

## Execução

```bash
# Sobe Kafka (KRaft) + PostgreSQL
docker-compose up -d

# Sobe a aplicação
mvn spring-boot:run
```

API disponível em `http://localhost:8080`.

> **Nota:** o PostgreSQL usa a porta 5434 para não conflitar com outras instâncias locais.

## Endpoints

### Simulação de eventos

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/simulate/order-placed` | Publica evento de pedido criado no Kafka |
| `POST` | `/api/v1/simulate/order-cancelled` | Publica evento de pedido cancelado no Kafka |

### Notificações

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/notifications` | Lista todas as notificações |
| `GET` | `/api/v1/notifications/order/{orderId}` | Notificações de um pedido específico |

## Exemplos

### Simular pedido criado

```bash
curl -X POST http://localhost:8080/api/v1/simulate/order-placed \
  -H "Content-Type: application/json" \
  -d '{"orderId": "ped-001", "customerId": "cliente-001"}'
```

```
Evento ORDER_PLACED publicado: ped-001
```

### Consultar notificações do pedido

```bash
curl http://localhost:8080/api/v1/notifications/order/ped-001
```

```json
[
  {
    "id": "6c575c36-32f6-4f59-95f1-d398a346eadb",
    "orderId": "ped-001",
    "customerId": "cliente-001",
    "type": "ORDER_PLACED",
    "message": "Seu pedido ped-001 foi recebido e esta sendo processado.",
    "status": "SENT",
    "createdAt": "2026-04-11T19:22:46"
  },
  {
    "id": "c46e4269-28e7-4658-ba02-dc1a05191b99",
    "orderId": "ped-001",
    "customerId": "cliente-001",
    "type": "ORDER_CANCELLED",
    "message": "Seu pedido ped-001 foi cancelado.",
    "status": "SENT",
    "createdAt": "2026-04-11T19:23:34"
  }
]
```

## Considerações para produção

- Substituir `ddl-auto=update` por migrations com Flyway ou Liquibase
- Adicionar Schema Registry (Confluent) para versionamento dos eventos com Avro
- Implementar idempotência no consumer para evitar processamento duplicado
- Monitorar lag de consumer groups com Kafka UI ou Prometheus + Grafana
- Mover configurações sensíveis para variáveis de ambiente

## Autor

LNL &nbsp; GitHub: [@leonlimask20-dot](https://github.com/leonlimask20-dot) &nbsp; Email: leonlimask@gmail.com
