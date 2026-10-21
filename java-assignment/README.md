# Fulfillment & Colocation Management Monolith

[![Java CI/CD Pipeline & Code Coverage](https://github.com/gaurp/fcs-interview-code-assignment/actions/workflows/ci.yml/badge.svg)](https://github.com/gaurp/fcs-interview-code-assignment/actions)
![Java Version](https://img.shields.io/badge/Java-17%20%7C%2021%20%7C%2025-blue.svg)
![Framework](https://img.shields.io/badge/Quarkus-3.13.3-red.svg)
![Coverage](https://img.shields.io/badge/Coverage-85.77%25%20(Line)-brightgreen.svg)
![Tests](https://img.shields.io/badge/Tests-94%20passed%2C%200%20failed-success.svg)

A high-performance Java/Quarkus application for managing Warehouses, Stores, Products, and geographical Locations with Clean Architecture, transactional integrity, and fulfillment unit routing rules.

---

## Table of Contents
1. [Overview & Completed Deliverables](#overview--completed-deliverables)
2. [Architecture & Design Principles](#architecture--design-principles)
3. [Prerequisites & Environment Setup](#prerequisites--environment-setup)
4. [Building, Testing & Code Coverage](#building-testing--code-coverage)
5. [CI/CD Pipeline](#cicd-pipeline)
6. [API Documentation & Endpoints](#api-documentation--endpoints)
7. [Case Study & Architectural Analysis](#case-study--architectural-analysis)

---

## Overview & Completed Deliverables

All core requirements and bonus tasks have been fully implemented and verified:

| Component | Task | Status | Description |
| :--- | :--- | :---: | :--- |
| **Location** | Task 1 | `COMPLETED` | Implemented `resolveByIdentifier` in `LocationGateway` to resolve location metadata (max warehouses, max capacity) by city prefix. Full unit test coverage in `LocationGatewayTest`. |
| **Store** | Task 2 | `COMPLETED` | Decoupled downstream `LegacyStoreManagerGateway` calls using Quarkus/CDI transaction-bound events (`@Observes(during = TransactionPhase.AFTER_SUCCESS)`). Legacy updates execute strictly after successful database commit. |
| **Warehouse** | Task 3 | `COMPLETED` | Implemented Clean Architecture (Domain Models, Use Cases, Ports & Adapters) with full validation rules for **Create**, **Archive**, and **Replace** operations. OpenAPI contract implementation with proper REST status codes (200, 201, 204, 400, 404). |
| **Fulfillment** | Task 4 (BONUS) | `COMPLETED` | Implemented Warehouse-to-Store product fulfillment associations with three core business constraints: max 2 warehouses per product per store, max 3 warehouses per store, max 5 product types per warehouse. Includes REST endpoints and full test suite. |
| **Questions** | Architectural Review | `COMPLETED` | Comprehensive senior-level architectural analysis documented in [`QUESTIONS.md`](QUESTIONS.md) (and [`Question.md`](Question.md)). |
| **Case Study** | Business & Technical Scenarios | `COMPLETED` | In-depth strategic and technical analyses covering all 5 scenarios documented in [`../case-study/CASE_STUDY.md`](../case-study/CASE_STUDY.md). |
| **CI/CD** | Pipeline & Quality Gate | `COMPLETED` | GitHub Actions workflow (`../.github/workflows/ci.yml`) enforcing automated test execution and **JaCoCo >80% line coverage gate**. |

---

## Architecture & Design Principles

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION / API LAYER                        │
│   • WarehouseResourceImpl (OpenAPI Spec-First JAX-RS)                 │
│   • StoreResource (JAX-RS + CDI Transaction-Bound Events)              │
│   • ProductResource (JAX-RS + Panache Repository)                     │
│   • FulfillmentResource (Bonus Association APIs)                      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Invokes
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         DOMAIN & APPLICATION LAYER                     │
│   • CreateWarehouseUseCase                                             │
│   • ReplaceWarehouseUseCase                                            │
│   • ArchiveWarehouseUseCase                                            │
│   • FulfillmentService                                                 │
│   • Pure Domain POJOs (Warehouse, Location, StoreCreatedEvent)         │
│   • Ports / Interfaces (WarehouseStore, LocationGateway)               │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Implements
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     INFRASTRUCTURE & PERSISTENCE LAYER                 │
│   • WarehouseRepository (Adapter implementing WarehouseStore)          │
│   • DbWarehouse (JPA Panache Entity)                                   │
│   • FulfillmentAssociationRepository (JPA Panache Repository)          │
│   • StoreSyncListener (CDI AFTER_SUCCESS Event Consumer)               │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Prerequisites & Environment Setup

- **Java Development Kit (JDK):** Version 17, 21, or 25.
- **Maven:** 3.8+ (or use the included `./mvnw` / `.\mvnw.cmd` wrapper).
- **Database:**
  - **For Tests:** Automated in-memory H2 (Postgres-compatibility mode) configured for test profile (`%test.quarkus.datasource.db-kind=h2`). Tests run standalone with zero external dependencies.
  - **For Production:** PostgreSQL 13+ (or Docker container).

---

## Building, Testing & Code Coverage

All commands should be executed from this directory:

### 1. Compile & Build
```bash
./mvnw clean compile
```

### 2. Run All Tests & Enforce 80% JaCoCo Coverage
```bash
./mvnw clean test jacoco:check
```

### 3. Package Application JAR
```bash
./mvnw package -DskipTests
```

### 4. Run Application Locally
```bash
# In Quarkus Development Mode (with Live Reload):
./mvnw quarkus:dev

# Or run conventional JAR (requires PostgreSQL or Docker):
java -jar ./target/quarkus-app/quarkus-run.jar
```

### 5. Test Suite & Coverage Metrics
- **Total Tests:** 94 passed, 0 failures, 0 errors.
- **JaCoCo Line Coverage:** **85.77%** (Exceeds the 80% requirement).
- JaCoCo HTML report generated at: `target/jacoco-report/index.html`.
- JaCoCo CSV report generated at: `target/jacoco-report/jacoco.csv`.

---

## CI/CD Pipeline

The GitHub Actions workflow is located at [`.github/workflows/ci.yml`](../.github/workflows/ci.yml).

### Pipeline Stages:
1. **Checkout Code:** Fetches repository code.
2. **Set up JDK 17:** Configures Eclipse Temurin OpenJDK 17 with Maven caching.
3. **Execute Test Suite & Quality Gate:** Runs `./mvnw clean test jacoco:check`. If line coverage drops below **80%**, the pipeline fails automatically.
4. **Package Verification:** Validates packaging with `./mvnw package -DskipTests`.
5. **Publish Step Summary:** Formats and prints JaCoCo line and instruction coverage directly into the GitHub Actions run summary.
6. **Artifact Archiving:** Uploads test reports (`surefire-reports`) and JaCoCo coverage reports as downloadable workflow artifacts (retained for 14 days).

---

## API Documentation & Endpoints

### 1. Warehouse Management (`/warehouse`)
Defined by OpenAPI 3.0 specification (`src/main/resources/warehouse-openapi.yaml`):

| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `GET` | `/warehouse` | Retrieve all active warehouses | `200 OK` |
| `POST` | `/warehouse` | Create a new warehouse with location and capacity validations | `201 Created`, `400 Bad Request` |
| `GET` | `/warehouse/{businessUnitCode}` | Retrieve active warehouse by Business Unit Code | `200 OK`, `404 Not Found` |
| `DELETE` | `/warehouse/{businessUnitCode}` | Archive warehouse by Business Unit Code | `204 No Content`, `404 Not Found` |
| `POST` | `/warehouse/replacement` | Replace active warehouse, archiving old and creating replacement with same BU code | `201 Created`, `400 Bad Request`, `404 Not Found` |

### 2. Store Management (`/store`)
| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `GET` | `/store` | List all stores | `200 OK` |
| `POST` | `/store` | Create store (triggers post-commit sync) | `201 Created` |
| `PATCH` | `/store/{id}` | Update store (triggers post-commit sync) | `200 OK`, `404 Not Found` |

### 3. Product Management (`/product`)
| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `GET` | `/product` | List all products | `200 OK` |
| `POST` | `/product` | Create a product | `201 Created` |
| `PUT` | `/product/{id}` | Update a product | `200 OK`, `404 Not Found` |
| `DELETE` | `/product/{id}` | Delete a product | `204 No Content`, `404 Not Found` |

### 4. Fulfillment Unit Associations (`/fulfillment` - BONUS)
| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `POST` | `/fulfillment/associate` | Associate warehouse as fulfillment unit for a product and store | `200 OK`, `400 Bad Request` |
| `POST` | `/fulfillment/disassociate`| Remove fulfillment association | `204 No Content`, `404 Not Found` |
| `GET` | `/fulfillment/store/{storeId}`| Get active warehouses fulfilling a store | `200 OK` |
| `GET` | `/fulfillment/warehouse/{buCode}`| Get product types stored in a warehouse | `200 OK` |

#### Fulfillment Association Constraints Enforced:
1. **Rule 1:** Each product can be fulfilled by a maximum of **2 different warehouses** per store.
2. **Rule 2:** Each store can be fulfilled by a maximum of **3 different warehouses**.
3. **Rule 3:** Each warehouse can store maximally **5 distinct types of products**.
