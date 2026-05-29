# Mini Lead CRM

A clean, production-style backend CRM application built using Spring Boot and Java 17.

This project was developed as a backend engineering assignment to demonstrate:

* REST API design
* layered architecture
* DTO-based API development
* business workflow implementation
* validation and exception handling
* bulk processing with partial success handling
* manual in-memory caching
* database integration using MySQL
* clean service-layer orchestration

---

# Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* Hibernate
* MySQL
* Maven
* Lombok
* Jakarta Validation
* ConcurrentHashMap-based in-memory caching

---

# Project Overview

Mini Lead CRM is a lightweight backend CRM system for managing leads and lead workflows.

The application supports:

* Lead CRUD operations
* Lead status workflow validation
* Bulk lead operations
* Partial-success bulk processing
* DTO validation
* Structured exception handling
* Manual in-memory caching
* MySQL persistence
* Clean layered architecture

The project intentionally avoids over-engineering and focuses on maintainable, production-style backend design suitable for internship-level backend engineering assignments.

---

# Project Structure

```text
src/main/java/com/sst/mini_lead_crm
│
├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── mapper
├── exception
├── config
├── enums
└── MiniLeadCrmApplication.java
```

---

# Architecture Overview

The project follows a standard layered architecture.

## Controller Layer

* Handles HTTP requests/responses
* Keeps endpoints thin and clean
* Delegates business logic to services

## Service Layer

* Contains core business logic
* Handles lead workflow rules
* Coordinates bulk operations
* Performs per-record validation for bulk APIs
* Manages cache orchestration and consistency

## Repository Layer

* Uses Spring Data JPA
* Handles database access
* Uses UUID-based entity identifiers

## DTO Layer

* Separates API contracts from entities
* Prevents exposing persistence models directly

## Mapper Layer

* Manual DTO ↔ entity mapping
* Keeps mapping logic explicit and simple

## Exception Layer

* Centralized exception handling using `@RestControllerAdvice`
* Structured JSON error responses

---

# Database Configuration

Database used:

* MySQL

## Create Database

```sql
CREATE DATABASE minicrm;
```

---

# Sample application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/minicrm
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update

    show-sql: true

    properties:
      hibernate:
        format_sql: true
```

---

# Features Implemented

# LEVEL 1

## Lead CRUD APIs

* Create Lead
* Get All Leads
* Get Lead By ID
* Update Lead
* Delete Lead

## Lead Status Workflow

Implemented controlled lead status transitions:

```text
NEW -> CONTACTED -> QUALIFIED -> CONVERTED
NEW -> LOST
CONTACTED -> LOST
QUALIFIED -> LOST
```

## Terminal States

* CONVERTED
* LOST

Invalid transitions are rejected using business-rule validation.

## Additional Features

* DTO validation
* Global exception handling
* MySQL integration
* Layered architecture
* UUID-based identifiers
* Enum-based workflow validation

---

# LEVEL 2

## Bulk Operations

Implemented:

* Bulk Create Leads API
* Bulk Update Leads API

## Partial Success Handling

Bulk APIs are designed so that:

* one failing record does NOT fail the entire request
* valid records continue processing
* failures are isolated per item

## Per-Record Validation

Bulk APIs support:

* DTO validation per record
* malformed UUID isolation
* business validation per item
* readable error responses

Example:

```json
{
  "success": false,
  "error": "name: Name must be between 1 and 100 characters"
}
```

## Bulk API Design Decisions

* Bulk APIs orchestrate existing single-record service methods
* Validation annotations are reused using `Validator`
* UUID parsing is handled manually for fault isolation
* Internal service logic still uses strong UUID typing

---

# LEVEL 3

## Manual In-Memory Caching

Implemented lightweight in-memory caching using:

```java
private final Map<UUID, LeadResponse> cache = new ConcurrentHashMap<>();
```

The cache is managed manually inside the service layer to keep the implementation explicit, easy to debug, and simple to explain during interviews.

## Cached Endpoint

```text
GET /leads/{id}
```

## Cache Miss Flow

1. Request arrives for a lead ID
2. Application checks cache
3. If cache entry is missing:

    * fetch lead from database
    * map entity to response DTO
    * store result in cache
    * return response

## Cache Hit Flow

1. Request arrives for a previously cached lead ID
2. Lead is returned directly from cache
3. Database query is skipped

## Cache Consistency Handling

### PUT /leads/{id}

* Updates database
* Refreshes cache entry with latest data

### PATCH /leads/{id}/status

* Updates database
* Refreshes cache entry with latest status

### DELETE /leads/{id}

* Deletes record from database
* Removes cache entry

## Why ConcurrentHashMap?

`ConcurrentHashMap` was chosen because it:

* provides thread-safe access
* supports concurrent requests safely
* remains lightweight and infrastructure-independent
* keeps the implementation simple and internship-assignment appropriate

## Design Decisions

The project intentionally avoids:

* Redis
* Spring Cache annotations
* external cache infrastructure

Manual cache management was chosen because it:

* demonstrates cache-aside strategy clearly
* keeps cache behavior explicit
* simplifies debugging
* improves interview explainability
* avoids unnecessary infrastructure complexity

---

# API Endpoints

| Method | Endpoint             | Description             |
| ------ | -------------------- | ----------------------- |
| POST   | `/leads`             | Create lead             |
| GET    | `/leads`             | Get all leads           |
| GET    | `/leads/{id}`        | Get lead by ID (cached) |
| PUT    | `/leads/{id}`        | Update lead             |
| DELETE | `/leads/{id}`        | Delete lead             |
| PATCH  | `/leads/{id}/status` | Update lead status      |
| POST   | `/leads/bulk`        | Bulk create leads       |
| PUT    | `/leads/bulk`        | Bulk update leads       |

---

# Caching Strategy

The application uses a manual cache-aside strategy.

## Cache Read Flow

```text
Client Request
       ↓
Check Cache
       ↓
Cache Hit → Return Cached Response
       ↓
Cache Miss → Fetch From Database
       ↓
Store In Cache
       ↓
Return Response
```

## Cache Update Strategy

Whenever lead data changes:

* cache entries are refreshed after updates
* cache entries are removed after deletions

This ensures cache consistency with database state.

---

# Example API Requests

# Create Lead

## Request

```http
POST /leads
Content-Type: application/json
```

## Request Body

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210",
  "source": "LinkedIn"
}
```

## Response

```json
{
  "id": "6f4db9f8-37b8-4b37-a7f4-6f5b0f5e1234",
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210",
  "status": "NEW",
  "source": "LinkedIn",
  "createdAt": "2026-05-29T12:00:00",
  "updatedAt": "2026-05-29T12:00:00"
}
```

---

# Bulk Create Example

## Request

```http
POST /leads/bulk
Content-Type: application/json
```

## Request Body

```json
[
  {
    "name": "Alice",
    "email": "alice@example.com"
  },
  {
    "name": "",
    "email": "invalid-email"
  },
  {
    "name": "Bob",
    "email": "bob@example.com"
  }
]
```

## Response

```json
{
  "total": 3,
  "successful": 2,
  "failed": 1,
  "results": [
    {
      "success": true,
      "data": {
        "name": "Alice"
      }
    },
    {
      "success": false,
      "error": "name: Name must be between 1 and 100 characters, email: Invalid email format"
    },
    {
      "success": true,
      "data": {
        "name": "Bob"
      }
    }
  ]
}
```

---

# Exception Handling

Global exception handling implemented using:

```java
@RestControllerAdvice
```

Handled exceptions include:

* ResourceNotFoundException
* InvalidStatusTransitionException
* BadRequestException
* Validation exceptions
* JSON parsing exceptions
* Generic server exceptions

---

# Status Transition Validation Example

Invalid transition example:

```text
CONVERTED -> NEW
```

Response:

```json
{
  "status": 400,
  "error": "Invalid Status Transition",
  "message": "Cannot transition lead status from CONVERTED to NEW"
}
```

---

# Sample Postman Test Scenarios

## Level 1

* Create lead
* Get all leads
* Get lead by ID
* Update lead
* Delete lead
* Update lead status
* Invalid status transition
* Validation failure
* 404 handling

## Level 2

* Fully successful bulk create
* Partially successful bulk create
* Bulk update with invalid UUID
* Bulk update with validation failures
* Mixed valid and invalid records
* Per-record error validation

## Level 3

* Cache miss verification
* Cache hit verification
* Cache refresh after update
* Cache refresh after status update
* Cache eviction after delete
* Re-fetch after cache eviction
* Concurrent cache access testing

---

# Engineering Practices Used

* Layered architecture
* DTO-based API design
* Constructor injection
* Transaction management
* Enum-based business workflow validation
* Manual DTO mapping
* Thin controllers
* Structured exception handling
* UUID-based identifiers
* Per-record validation orchestration
* Fault isolation in bulk APIs
* Manual cache-aside strategy
* Thread-safe in-memory caching
* Explicit cache invalidation
* Cache consistency management

---

# Running the Project Locally

## 1. Clone Repository

```bash
git clone <repository-url>
```

---

## 2. Configure MySQL

Create database:

```sql
CREATE DATABASE minicrm;
```

---

## 3. Configure application.yml

Update:

* database URL
* username
* password

---

## 4. Run Application

Using Maven:

```bash
mvn spring-boot:run
```

Application runs on:

```text
http://localhost:8080
```

---

# Future Improvements

Potential future enhancements:

* Pagination and sorting
* Search APIs
* Swagger/OpenAPI documentation
* Authentication & authorization
* Redis/distributed caching
* Cache TTL and eviction strategies
* Docker support
* Unit and integration testing
* Audit logging
* Role-based access control

---

# Author

Arnavya Chettri
