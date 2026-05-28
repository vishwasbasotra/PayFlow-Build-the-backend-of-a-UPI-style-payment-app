# PayFlow — UPI-Style Payment Backend REST API

Welcome to **PayFlow**, a simplified high-performance backend REST API designed to power real-time UPI-style transactions. This project registers users, manages virtual wallet balances, and persists records of money transfers between users.

---

## 1. How to Run the Application

### Prerequisites
* **Java Development Kit (JDK) 21** or higher.
* An IDE (such as **IntelliJ IDEA**, **Eclipse**, or **VS Code**) with Spring Boot support.

### Option A: Running from an IDE (Recommended)
1. Open your IDE and import the project as a **Maven project**.
2. Navigate to `src/main/java/com/payflow/PayFlowApplication.java`.
3. Right-click the file and select **Run 'PayFlowApplication.main()'**.

### Option B: Running from the Command Line
Ensure your `JAVA_HOME` environment variable is correctly pointed to your JDK 21 installation, then execute the following commands in the project root:

* **On Windows (PowerShell):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
* **On Linux / macOS / Git Bash:**
  ```bash
  chmod +x mvnw
  ./mvnw spring-boot:run
  ```

Once started, the application will run locally at: `http://localhost:8080`

---

## 2. Project Architecture and Layers

PayFlow is organized using the standard **Layered Architecture (3-Tier)** to separate concerns, maintain clean code, and decouple database structures from request-handling logic.

```
       [ HTTP Clients (Postman / Curl) ]
                     │
                     ▼
┌─────────────────────────────────────────────┐
│              Controller Layer               │  <-- Manages REST endpoints, request binding
└─────────────────────┬───────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────┐
│                Service Layer                │  <-- Executes core business logic
└─────────────────────┬───────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────┐
│              Repository Layer               │  <-- Interacts with H2 Database via JPA
└─────────────────────┬───────────────────────┘
                      │
                      ▼
            [ H2 In-Memory Database ]
```

### The Four Core Layers

1. **Entity Layer (`com.payflow.entity`)**
   * **Role:** Defines the blueprint of our domain model and structures database tables using JPA (`@Entity`) annotations.
   * **Specifically in PayFlow:** Holds the `User` and `Transaction` classes, representing the `users` and `transactions` tables in the database. It utilizes Hibernate validations (e.g. `@NotBlank`, `@DecimalMin`) to maintain strict data integrity before writing to the database.

2. **Repository Layer (`com.payflow.repository`)**
   * **Role:** Serves as the data access abstraction layer. By extending `JpaRepository`, it provides out-of-the-box CRUD operations without requiring boilerplate SQL.
   * **Specifically in PayFlow:** `UserRepository` and `TransactionRepository` abstract database transactions, enabling operations like saving data, finding by primary key, or running custom queries.

3. **Service Layer (`com.payflow.service`)**
   * **Role:** The brain of the application. It acts as a middleman between the Controller and the Repository, housing core business and validation rules.
   * **Specifically in PayFlow:** `UserServiceImplementation` and `TransactionServiceImplementation` enforce rules (e.g., checking if a user already exists before registration, or verifying that a sender has enough balance before processing a transaction).

4. **Controller Layer (`com.payflow.controller`)**
   * **Role:** The entry point for incoming HTTP requests. It maps request paths, parses JSON request bodies (using `@RequestBody`), triggers service operations, and returns standardized JSON responses with proper HTTP status codes.
   * **Specifically in PayFlow:** `UserController` and `TransactionController` handle incoming endpoints (such as `POST /users` and `POST /transactions`), exposing the API to the outside world.

---

## 3. Core Spring Boot Features in PayFlow

Spring Boot simplifies enterprise application development through three main pillars:

### A. Embedded Server
* **What it is:** Traditional Spring applications required configuring and deploying a WAR file to a separate web server (like Apache Tomcat or WildFly). Spring Boot bundles the servlet container directly inside the application JAR.
* **In PayFlow:** When you run `PayFlowApplication.java`, you will see console logs showing `Tomcat initialized with port 8080`. Spring Boot spins up an internal Apache Tomcat server instantly. This eliminates external server setups and guarantees that the application runs identically on any environment.

### B. Auto-Configuration
* **What it is:** Spring Boot attempts to automatically configure Spring beans based on the libraries added to the classpath. It uses a "convention over configuration" approach.
* **In PayFlow:** Because H2 database and `spring-boot-starter-data-jpa` are in the classpath, Spring Boot automatically creates a `DataSource` connected to the in-memory database, builds the `EntityManagerFactory`, configures the transaction managers, and enables the H2 console at `/h2-console` without requiring a single line of XML configuration.

### C. Production-Ready Defaults (Starter Dependencies)
* **What it is:** "Starters" are a set of convenient dependency descriptors that bundle related libraries together under a single dependency.
* **In PayFlow:** Instead of listing dozens of individual JAR files for Spring MVC, Jackson (JSON parser), Logging (Logback), and Validation, we import `spring-boot-starter-webmvc`. This imports pre-configured, tested, and optimized production-grade defaults, saving developers from "dependency hell."

---

## 4. Database Schema Auto-Generation

With `spring.jpa.show-sql=true` active, Spring Data JPA automatically compiles entity annotations and instructs Hibernate to create the tables in the in-memory H2 database on startup.

### User Table Creation SQL
```sql
create table users (
    balance double not null,
    user_id bigint generated by default as identity,
    name varchar(20),
    phone_number varchar(10),
    upi_id varchar(255),
    primary key (user_id)
)
```

### Transaction Table Creation SQL
```sql
create table transactions (
    amount double not null,
    transaction_id bigint generated by default as identity,
    note varchar(255),
    receiver_upi_id varchar(255),
    sender_upi_id varchar(255),
    primary key (transaction_id)
)
```

> **Note on Naming Conventions:** Notice how camelCase field names in Java (e.g., `userId`, `phoneNumber`, `senderUpiId`, `receiverUpiId`) are automatically converted into snake_case column names (`user_id`, `phone_number`, `sender_upi_id`, `receiver_upi_id`) in H2. This is the default JPA Physical Naming Strategy working behind the scenes.

---

## 5. Repository Layer & JPA Derived Queries

In `UserRepository`, we have defined the following derived query method:
```java
User findByUpiId(String upiId);
```

### Generated SQL Statement
When this method is called, Spring Data JPA translates it into the following SQL query:
```sql
select 
    u1_0.user_id, 
    u1_0.balance, 
    u1_0.name, 
    u1_0.phone_number, 
    u1_0.upi_id 
from users u1_0 
where u1_0.upi_id=?
```

### Analysis of the Derived Query

#### (a) How JPA derives it from the method name:
Spring Data JPA uses a **Query Creation DSL** to parse the method name. 
1. It splits the method name starting at the prefix `findBy`.
2. It looks at the remaining part, `UpiId`, and matches it against properties defined in the `User` entity (resolving it to the `upiId` variable).
3. It determines the return type (`User`), maps the query to the `users` table, and automatically writes a `SELECT` query utilizing a `WHERE` clause: `WHERE upi_id = ?`.

#### (b) What the `?` placeholder means:
The `?` character is a **positional parameter placeholder** (specifically, a Prepared Statement parameter). 
* **Safe Binding:** Instead of concatenating string values directly into the query (which exposes the system to highly dangerous **SQL Injection** attacks), the database compiles the query structure beforehand.
* **Performance:** The prepared statement is pre-compiled and cached by the database engine. At runtime, the value passed to the method (e.g., `"priya@okaxis"`) is bound safely to the placeholder as plain data, ensuring security and speed.

---

## 6. Comparison of Custom Query Approaches

When Spring Data JPA's built-in CRUD operations are not enough, developers can use three approaches to fetch data:

| Aspect | 1. Derived Method Names | 2. `@Query` with JPQL | 3. Native SQL Queries |
| :--- | :--- | :--- | :--- |
| **Syntax Basis** | Method Name Parsing Rules | Java Entities & Fields | Raw SQL Database Tables & Columns |
| **Database Portability** | **High** (Auto-translated to any SQL dialect) | **High** (Database agnostic; handled by JPA provider) | **Low** (Tied directly to a specific SQL dialect) |
| **Complexity Level** | Low (Single-column filters, basic logical operators) | Medium-High (Complex joins, groupings, aggregate math) | Unlimited (Allows vendor-specific features/queries) |
| **Type & Compile-Time safety** | **Yes** (Parsed at startup; throws errors if property names mismatch) | **Yes** (Validated at application startup against Java entity classes) | **No** (Plain text strings; SQL syntax errors are caught only at runtime) |

### Why Native Queries are the Least Preferred
1. **Loss of Database Independence (Vendor Lock-in):** Native SQL bypasses Hibernate's dialect abstraction. If a query uses specialized features (like H2's `LIMIT` or PostgreSQL's json operators), switching the underlying database (e.g., migrating from H2 to PostgreSQL or Oracle) will break the query.
2. **Loss of Object-Oriented Semantics:** JPQL queries operate on Java objects and attributes, respecting relationships and mappings. Native SQL requires working directly with tables and column names, bypassing the object model and forcing developer overhead in manually mapping native result sets to objects.
3. **No Startup Validation:** Spring Boot parses JPQL and Derived Queries on startup to ensure entity fields match. Native SQL strings are ignored during validation, leaving syntax mistakes or renamed column bugs to fail silently until the code executes at runtime.

---

## 7. Task 5 — REST Endpoints & `@RequestBody` Demonstration

### Endpoint Testing and Curl Logs

Below are the standard `curl` commands and expected JSON responses to verify all four major REST endpoints.

#### 1. Register User (`POST /api/users`)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Priya","upiId":"priya@okaxis","balance":1500.00,"phoneNumber":"9876543210"}'
```
**JSON Response:**
```json
{
  "userId": 1,
  "name": "Priya",
  "upiId": "priya@okaxis",
  "balance": 1500.00,
  "phoneNumber": "9876543210"
}
```

#### 2. List All Users (`GET /api/users`)
```bash
curl http://localhost:8080/api/users
```
**JSON Response:**
```json
{
  "content": [
    {
      "userId": 1,
      "name": "Priya",
      "upiId": "priya@okaxis",
      "balance": 1500.00,
      "phoneNumber": "9876543210"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "lastPage": true
}
```

#### 3. Look Up User by ID (`GET /api/users/{id}`)
```bash
curl http://localhost:8080/api/users/1
```
**JSON Response:**
```json
{
  "userId": 1,
  "name": "Priya",
  "upiId": "priya@okaxis",
  "balance": 1500.00,
  "phoneNumber": "9876543210"
}
```

#### 4. Send Money / Record Transaction (`POST /api/transactions`)
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"senderUpiId":"priya@okaxis","receiverUpiId":"rahul@okaxis","amount":500.00,"note":"Dinner Split"}'
```
**JSON Response:**
```json
{
  "transactionId": 1,
  "amount": 500.00,
  "senderUpi": "priya@okaxis",
  "receiverUpi": "rahul@okaxis",
  "note": "Dinner Split",
  "balance": 1000.00
}
```

---

### `@RequestBody` Binding Demonstration & Verification

To verify the role of Spring Web's `@RequestBody` annotation, we compared two scenarios inside the `UserController` by printing the bound `UserDTO` parameter.

#### Scenario A: WITH `@RequestBody`
* **Controller Signature:** `public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO)`
* **Terminal Command:**
  ```bash
  curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name":"Priya","upiId":"priya@okaxis","balance":1500.00,"phoneNumber":"9876543210"}'
  ```
* **Console Print (`System.out.println`):**
  ```text
  Bound UserDTO object: UserDTO(userId=null, name=Priya, upiId=priya@okaxis, balance=1500.0, phoneNumber=9876543210)
  ```

#### Scenario B: WITHOUT `@RequestBody`
* **Controller Signature:** `public ResponseEntity<UserDTO> registerUser(@Valid UserDTO userDTO)`
* **Terminal Command:**
  ```bash
  curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name":"Priya","upiId":"priya@okaxis","balance":1500.00,"phoneNumber":"9876543210"}'
  ```
* **Console Print (`System.out.println`):**
  ```text
  Bound UserDTO object: UserDTO(userId=null, name=null, upiId=null, balance=null, phoneNumber=null)
  ```

#### Why the Fields are Null Without `@RequestBody`

When an HTTP client sends a `POST` request with a JSON payload, the data resides in the raw body of the HTTP request. Without the **`@RequestBody`** annotation on the controller's handler method parameter, Spring Web assumes standard query parameter or form-data binding (using servlet request parameters). Because the JSON payload is in the request body and not formatted as URL query parameters, Spring's model-binder is unable to find matching keys, leaving the Java `User` object instantiated via its default constructor but with all its fields remaining `null`. Adding `@RequestBody` instructs Spring to invoke an `HttpMessageConverter` (such as Jackson) to read the raw request body stream, parse the JSON, and map those keys directly to the Java object's fields.

