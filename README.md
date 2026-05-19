# Notification System

A Spring Boot notification system that accepts messages by category and asynchronously delivers them to subscribed users through their configured notification channels.

The project demonstrates clean architecture, asynchronous job processing, database-backed audit logs, validation, centralized exception handling, Flyway migrations, caching, and automated tests.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Runtime Flow](#runtime-flow)
- [Queue and Worker Processing](#queue-and-worker-processing)
- [Database](#database)
- [Local Setup](#local-setup)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [UI](#ui)
- [Validation](#validation)
- [Error Handling](#error-handling)
- [Correlation ID](#correlation-id)
- [Caching](#caching)
- [Running Tests](#running-tests)
- [Design Highlights](#design-highlights)
- [Future Improvements](#future-improvements)

---

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring MVC
- Spring Data JPA
- Jakarta Validation
- PostgreSQL
- Flyway
- Thymeleaf
- Caffeine Cache
- JUnit 5
- Mockito
- Testcontainers
- Maven

---

## Features

### Supported notification categories

- `SPORTS`
- `FINANCE`
- `MOVIES`

### Supported notification channels

- `EMAIL`
- `SMS`
- `PUSH`

The notification sender implementations simulate successful delivery and are structured so real external providers can be added later.

---

## Architecture Overview

The application follows a clean layered architecture:
```text
Web Layer
↓
Application Layer
↓
Domain Layer
↓
Infrastructure Layer
↓
Database / Queue / Cache
```
### Layer responsibilities

| Layer | Responsibility |
| --- | --- |
| Web | REST controllers, UI controller, request validation entry points |
| Application | Coordinates notification submission and query use cases |
| Domain | Business models, ports, dispatching, job processing, sender registry |
| Infrastructure | JPA repositories, persistence adapters, senders, queue workers, cache |

The domain and application layers depend on interfaces/ports instead of concrete infrastructure implementations.

---

## Runtime Flow

### Notification submission
```text
Client
↓
POST /api/notifications
↓
NotificationController
↓
NotificationCommandService
↓
Persist notification message
↓
Create PENDING notification job
↓
Persist notification job and make it available to the configured worker mode
↓
Return 202 Accepted
```
### Background processing
```text
Worker
↓
Fetch pending jobs
↓
NotificationJobProcessor
↓
Mark job PROCESSING
↓
Load message and subscribers
↓
NotificationDispatcher
↓
Select sender by channel
↓
Create notification log records
↓
Mark job PROCESSED or FAILED
```
A failure in one delivery attempt does not stop the rest of the notification dispatch process. Each user/channel delivery is recorded independently.

---

## Queue and Worker Processing

The application supports configurable notification job processing.

Default mode:
```yaml
notification:
    queue:
      processing-mode: database
```
Supported processing modes:

| Mode | Description |
| --- | --- |
| `database` | Workers fetch persisted pending jobs from the database |
| `memory` | Jobs can be pushed through the in-memory queue abstraction |

Database mode is preferred for reliability because jobs remain persisted if the application restarts.

---

## Database

Flyway migrations are located in:
```
src/main/resources/db/migration
```
Current migrations:
```
V1__create_notification_schema.sql
V2__seed_notification_catalogs.sql
V3__seed_notification_users.sql
V4__add_notification_correlation_id.sql
V5__add_notification_job_worker_indexes.sql
```
Main tables:

| Table | Purpose |
| --- | --- |
| `users` | Notification recipients |
| `categories` | Notification category catalog |
| `notification_channels` | Notification channel catalog |
| `user_category_subscriptions` | User/category subscriptions |
| `user_channel_preferences` | User/channel preferences |
| `messages` | Submitted notification messages |
| `notification_jobs` | Asynchronous processing jobs |
| `notification_logs` | Historical delivery audit records |

Notification logs store recipient snapshot data so historical audit records remain accurate even if user data changes later.

---

## Local Setup

### Prerequisites

- Java 21
- Docker
- Maven Wrapper included in the project

### Start PostgreSQL
```bash
docker compose up -d
```
### Run the application
```bash
./mvnw spring-boot:run
```
Application URL:
```text
http://localhost:8080
```
Notification UI:
```text
http://localhost:8080/notifications
```
---

## Configuration

Default local database configuration:
```yaml
spring:
   datasource:
      url: jdbc:postgresql://localhost:5432/notification
      username: notification
      password: notification
```
Worker configuration:
```yaml
notification:
   worker:
    enabled: true
    fixed-delay-ms: 1000
    batch-size: 20
    stale-timeout-minutes: 5
    recovery-fixed-delay-ms: 30000
    recovery-batch-size: 20
    retry-fixed-delay-ms: 60000
    retry-batch-size: 20
    retry-max-attempts: 3
```
---

## API Endpoints

### Submit notification
```http
POST /api/notifications
Content-Type: application/json
```
Request:
```json
{
    "category": "SPORTS",
    "message": "Game starts tonight!"
}
```
Successful response:
```http
202 Accepted
```
Example response:
```json
{
    "messageId": 1,
    "jobId": 1,
    "status": "ACCEPTED",
    "detail": "Notification job accepted for background processing.",
    "correlationId": "..."
}
```
### Get notification logs
```http
GET /api/notification-logs?page=0&size=20
```
```bash
 curl "http://localhost:8080/api/notification-logs?page=0&size=20"
```
Returns notification delivery logs sorted newest first.

### Get notification job status
```http
GET /api/notification-jobs/{jobId}
```
Example response:
```json
{
    "jobId": 1,
    "messageId": 1,
    "status": "PROCESSED",
    "attemptCount": 1,
    "lastError": null,
    "createdAt": "2026-05-18T10:00:00",
    "processedAt": "2026-05-18T10:00:01"
}
```
### Get categories
```http
GET /api/categories
```
```bash
 curl http://localhost:8080/api/categories
````

### Get notification channels
```http
GET /api/notification-channels
```
```bash 
 curl http://localhost:8080/api/notification-channels
```
---

## UI

The project includes a simple Thymeleaf UI:
```http
GET /notifications
```
The page allows users to:

- Select a category.
- Submit a notification message.
- View notification logs.

Because processing is asynchronous, logs may appear shortly after the request is accepted.

---

## Validation

Notification submission validates that:

- `category` is required.
- `message` is required.
- `message` must not be blank.
- `message` must not exceed the configured maximum length.
- The selected category must exist and be active.

Validation errors return:
```http
400 Bad Request
```
---

## Error Handling

The application includes centralized exception handling for:

- Bean validation errors
- Unknown or inactive categories
- Unsupported notification channels
- Queue failures
- Unexpected server errors

---

## Correlation ID

Requests can provide a correlation ID using the following header:
```http
X-Correlation-Id: your-correlation-id
```
The correlation ID is propagated to messages, jobs, logs, and application log output where applicable.

---

## Caching

Caffeine cache is used for read-heavy reference data.

Cached candidates include:

- Categories
- Notification channels
- Subscribers by category

Write-heavy records are not cached:

- Messages
- Notification jobs
- Notification logs

---

## Running Tests

### Unit tests
```bash
./mvnw test
```
### Integration tests

Integration tests use Testcontainers with PostgreSQL.
```bash
./mvnw verify
```
Integration test coverage includes:

- Repository persistence behavior
- Notification job queries
- Notification flow from HTTP request to processed job and generated logs

---

## Future Improvements

- Wrap external notification provider calls with Resilience4j circuit breakers per channel.
    - Use separate circuit breakers for `EMAIL`, `SMS`, and `PUSH` providers.
    - Fail fast when a provider is unavailable or slow.
    - Protect background worker threads from provider timeouts.
    - Allow the existing retry/recovery flow to retry failed deliveries later.
- Add timeout and retry policies around provider calls, tuned per notification channel.
- Add RabbitMQ or another durable external queue for distributed asynchronous processing.
- Add Redis for distributed caching.
- Add authentication and authorization for administrative or internal endpoints.
- Add metrics, tracing, and dashboards for worker throughput, job failures, provider failures, and circuit breaker state.
- Add idempotency handling for repeated notification submissions.
- Add administrative endpoints for failed-job replay and operational support.


