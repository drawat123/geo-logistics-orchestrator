### **Day 1: The Foundation & Concurrency Control**

**Goal:** Build the robust data layer that can handle financial data and race conditions (e.g., two requests trying to
book the same driver simultaneously).

#### **Task 1.1: Project Initialization**

* **Stack:** Java 21, Spring Boot 3.2+.
* **Dependencies:** Spring Web, Spring Data JPA, PostgreSQL Driver, Lombok, Validation (Hibernate Validator).
* **Database:** Set up a local PostgreSQL database named `geologistics_db`.

#### **Task 1.2: Domain Entity Design**

Create the following entities in a `model` package. Adhere strictly to these requirements to pass the review:

1. **Entity**: `Driver`

* Must have a unique ID.
* Must have a `driverStatus` (AVAILABLE, BUSY, OFFLINE). *Hint: Use an Enum.*
* Must have `latitude` and `longitude` to track position.
* **CRITICAL Requirement:** We need to prevent "Race Conditions." Add an **Optimistic Locking** field so that if two
  threads try to update the driver's driverStatus at the same time, one fails. *Hint: Look up `@Version`.*


2. **Entity**: `Order`

* Must have a unique ID.
* Must have an `orderValue` (Price). *Requirement: Do not use `double` or `float`. Use the correct data type for
  financial calculations.*
* Must be linked to a `Driver` (Many-to-One relationship). This field should be nullable (an order might not have a
  driver yet).
* Must have a timestamp for `createdAt`.

#### **Task 1.3: The Repository Layer**

Create the repositories. I want you to write **one custom JPQL query** in the `DriverRepository`.

* **Query Requirement:** specific method that finds all drivers who are `AVAILABLE` and currently stored in the
  database.