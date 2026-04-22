# 🏫 Smart Campus — Sensor & Room Management API

A robust, scalable RESTful API built with **Jakarta RESTful Web Services (JAX-RS)** for the University of Westminster "Smart Campus" initiative. This system manages thousands of campus rooms and their diverse sensor arrays using a high-performance, thread-safe in-memory data store.

---

## 🛠️ API Design Overview
The API is designed following the **Richardson Maturity Model (Level 3)**, emphasizing:
- **Resource Hierarchy**: A logical mapping of physical campus structures (Rooms -> Sensors -> Readings).
- **Statelessness**: Every request contains all necessary information, ensuring scalability.
- **HATEOAS**: The root discovery endpoint provides dynamic links for self-documentation.
- **Resilience**: A global exception mapping system that prevents internal data leaks (stack traces) and provides meaningful JSON error feedback.

---

## 🚀 How to Build & Launch

### Prerequisites
- **Java 11 or higher** (Tested up to Java 21)
- **Maven 3.9.x**
- **Apache Tomcat 9** (Required for manual deployment)

### Option 1: Using NetBeans (Recommended for Development)
1. Open NetBeans and select **File > Open Project**.
2. Navigate to the project folder and click **Open**.
3. Right-click the project name in the left panel and select **Properties**.
4. Go to **Run** and ensure a server (like Tomcat 9) is selected. Set the Context Path to `/`.
5. Click the **Run** button (green arrow) in the toolbar.
6. The API will be available at: `http://localhost:8080/api/v1`

### Option 2: Terminal — Maven Shortcut (Fastest)
This method uses an embedded Tomcat runner. You don't need to install Tomcat separately.
1. Open your terminal in the project root.
2. Build the project:
   ```bash
   mvn clean package
   ```
3. Launch the server:
   ```bash
   mvn tomcat7:run
   ```
4. Access the API at: `http://localhost:8080/api/v1`

### Option 3: Manual Deployment (Production Style)
1. Build the WAR file:
   ```bash
   mvn clean package
   ```
2. Copy the generated file `target/Smart-Campus-API.war` into your Tomcat installation's `webapps/` directory.
3. Rename the file to `ROOT.war` if you want it at the root, or keep the name to access it at `/Smart-Campus-API`.
4. Start Tomcat using `bin/startup.bat`.

---

## 🧪 Sample Interactions (curl)

### 1. Discover API Entry Point
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a New Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'
```

### 3. Register a Sensor to the Room
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "roomId": "LIB-301"}'
```

### 4. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 5. Append a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value": 22.5}'
```

---

## 📝 Conceptual Report (Coursework Answers)

### Part 1: Service Architecture
**Q1.1: JAX-RS Resource Lifecycle**
By default, JAX-RS resources use a **per-request** lifecycle. A new instance is created for every incoming request to ensure statelessness and prevent memory leaks. To prevent data loss, we use a **Singleton DataStore** (with `ConcurrentHashMap`) that persists throughout the application's lifetime, ensuring all requests share a single, thread-safe state.

**Q1.2: HATEOAS & Hypermedia**
Hypermedia links allow for "self-discovery," making the API resilient to URI changes and reducing coupling. Client developers can follow links (like `"rooms" -> "/api/v1/rooms"`) instead of relying on hardcoded strings or static documentation.

### Part 2: Room Management
**Q2.1: IDs vs. Full Objects in Lists**
Returning only IDs reduces network bandwidth but forces "N+1" requests to fetch details. Returning full objects is heavier on the network but allows the client to render all information in a single pass, which is better for dashboards.

**Q2.2: Idempotency of DELETE**
In this implementation, DELETE is **idempotent**. The first request deletes the room. Subsequent requests return a `404 Not Found`. While the status code changes, the **state of the server** (the room is gone) remains the same, satisfying the definition of idempotency.

### Part 3: Sensor Operations
**Q3.1: Content-Type Mismatch**
The `@Consumes(MediaType.APPLICATION_JSON)` annotation acts as a filter. If a client sends `text/plain`, JAX-RS rejects the request before it hits the method, returning an **HTTP 415 Unsupported Media Type** error.

**Q3.2: Query Parameters vs. Path Segments**
Query parameters (`?type=CO2`) are superior for filtering because they are optional and do not change the base resource identification. Path segments imply a hierarchical relationship that doesn't exist for search filters.

### Part 4: Sub-Resources
**Q4.1: Benefits of Sub-Resource Locators**
Sub-resource locators allow us to delegate logic to separate classes (like `SensorReadingResource`), keeping files small and focused. This prevents "God Classes" and improves modularity in large APIs.

### Part 5: Error Handling & Logging
**Q5.2: HTTP 422 vs. 404**
HTTP 422 (Unprocessable Entity) is more accurate when the JSON payload is valid but refers to a non-existent parent ID. A 404 typically implies the entire URL is wrong, while 422 indicates a business logic/referential integrity failure.

**Q5.4: Cybersecurity Risks of Stack Traces**
Exposing stack traces reveals internal class names, library versions, and file paths. Attackers can use this to identify known CVEs or understand internal logic to craft targeted exploits like SQL injection.

**Q5.5: Benefits of JAX-RS Filters**
Filters encapsulate cross-cutting concerns (logging) in a single place. This ensures consistency (DRY principle) and prevents cluttering business logic with repetitive logging statements.

---

## 📺 Video Demonstration
The Postman walkthrough video is uploaded directly to BlackBoard.
- **Scope**: All CRUD operations, type filtering, error handling (403, 404, 409, 422, 500).
