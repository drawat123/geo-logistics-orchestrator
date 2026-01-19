# 🌍 GeoLogistics Orchestrator

> A high-performance logistics backend combining Spring Boot architecture with custom Graph Algorithms.

## 📖 Overview

GeoLogistics Orchestrator is a backend system designed to manage intelligent routing and order dispatching for a
delivery network. Unlike standard CRUD applications, this project implements **core Graph Theory algorithms (Dijkstra)**
from scratch to solve the "Shortest Path" problem efficiently without relying on external mapping libraries.

**Current Status:** Phase 2 (Routing Engine & Persistence) Completed.

## 🛠 Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.2
* **Database:** PostgreSQL (with JPA/Hibernate)
* **Core Concepts:** * Graph Theory (Adjacency Lists, Weighted Graphs)
    * Algorithms (Dijkstra’s Shortest Path via PriorityQueue)
    * Concurrency (Optimistic Locking for Driver locations)

## 🚀 Key Features

### 1. Custom Routing Engine (DSA Implementation)

Instead of using Google Maps API, I built a routing engine manually:

* **In-Memory Graph:** Implements a directed weighted graph using `HashMap` (Adjacency List pattern).
* **Dijkstra's Algorithm:** Custom implementation using a `PriorityQueue` (Min-Heap) to calculate the shortest path
  between city nodes with $O(E \log V)$ time complexity.
* **Performance:** Optimized node lookups from $O(N)$ to $O(1)$ using a split Registry/Adjacency architecture.

### 2. Robust Persistence Layer

* **Optimistic Locking:** Uses `@Version` on `Driver` entities to prevent race conditions during concurrent location
  updates.
* **JPA Auditing:** Automated `@CreatedDate` tracking for Order history.

## 📦 Getting Started

### Prerequisites

* Java 21
* PostgreSQL
* Maven

### Configuration

Update `src/main/resources/application.properties` with your DB credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/geologistics
spring.datasource.username=postgres
spring.datasource.password=yourpassword