# 🗓️ Smart Campus API — 5-Day Development Plan

**Module:** 5COSC022W — Client-Server Architectures  
**Due:** 24th April 2026, 13:00  
**Stack:** Java + JAX-RS (Jersey) + Maven + Standalone Tomcat (WAR deployment)  
**Rules:** No Spring Boot · No databases · In-memory only (HashMap / ArrayList)

---

## 📊 Summary Table

| Day | Date     | Focus                           | Marks Covered  | Status  |
|-----|----------|---------------------------------|----------------|---------|
| 1   | Apr 17   | Setup + Discovery Endpoint      | Part 1 (10)    | ✅ COMPLETED |
| 2   | Apr 18   | Room Management                 | Part 2 (20)    | ✅ COMPLETED |
| 3   | Apr 19   | Sensor Management + Filtering   | Part 3 (20)    | ✅ COMPLETED |
| 4   | Apr 20   | Sub-Resources + Error Handling  | Part 4+5 (50)  | ✅ COMPLETED |
| 5   | Apr 21   | Deploy to Tomcat + Postman + README | All parts      | ✅ COMPLETED |

> **Buffer days:** Apr 22–23 for bug fixes and final polish  
> **Hard deadline:** Apr 24, 13:00 ✅

---

## ✅ Day 1 — April 17 (Thu): Project Setup + Discovery Endpoint
> **Part 1 — 10 Marks**

**Goal:** Get a running Maven project with a working `/api/v1` base URL.

### Tasks
- [ ] Bootstrap Maven project with Jersey + Standalone Tomcat (WAR packaging)
- [ ] Write `pom.xml` with:
  - `packaging` set to `war`
  - `jersey-container-servlet` — Jersey servlet integration
  - `jersey-media-json-jackson` — JSON support
  - `jersey-hk2` — Jersey dependency injection
  - `javax.servlet-api` with `scope: provided` (Tomcat ships it)
- [ ] Create `SmartCampusApplication.java` extending `javax.ws.rs.core.Application` with `@ApplicationPath("/api/v1")`
- [ ] Create minimal `src/main/webapp/WEB-INF/web.xml` (triggers Servlet 3.0 auto-scan — no `Main.java` needed)
- [ ] Create POJO models with full getters/setters:
  - `Room.java` — id, name, capacity, sensorIds (List)
  - `Sensor.java` — id, type, status, currentValue, roomId
  - `SensorReading.java` — id (UUID), timestamp (epoch ms), value
- [ ] Create `DataStore.java` — singleton class with:
  - `HashMap<String, Room> rooms`
  - `HashMap<String, Sensor> sensors`
  - `HashMap<String, List<SensorReading>> readings`
- [ ] Create `DiscoveryResource.java` at `@Path("/")`
  - `GET /api/v1` → returns JSON: API version, admin contact, links map
- [ ] Verify build works: `mvn clean package` → generates `target/*.war`

### Commit Messages
```
feat: initialize Maven WAR project with Jersey and Tomcat servlet container
feat: add web.xml and SmartCampusApplication with @ApplicationPath
feat: add Room, Sensor, SensorReading POJO models
feat: add DataStore singleton for in-memory storage
feat: implement GET /api/v1 discovery endpoint
```

### Report Questions (Part 1)
> **Q1.1:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created per request, or is it a singleton? How does this affect in-memory data synchronization?
>
> **Q1.2:** Why is HATEOAS (Hypermedia as the Engine of Application State) considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?

---

## ✅ Day 2 — April 18 (Fri): Room Management
> **Part 2 — 20 Marks**

**Goal:** Full CRUD for Rooms, with a sensor-guard safety check on DELETE.

### Tasks
- [ ] Create `RoomResource.java` registered at `@Path("/rooms")`
- [ ] `GET /api/v1/rooms` → return all rooms as a JSON array
- [ ] `POST /api/v1/rooms` → create a new room, return `201 Created` with the created room body
- [ ] `GET /api/v1/rooms/{roomId}` → return specific room or `404 Not Found`
- [ ] `DELETE /api/v1/rooms/{roomId}`:
  - Room not found → `404 Not Found`
  - Room has sensors in its list → throw `RoomNotEmptyException` (mapped to `409` — wired up on Day 4)
  - Success → `204 No Content`
- [ ] Register `RoomResource` in `SmartCampusApplication`

### Commit Messages
```
feat: implement GET /api/v1/rooms and POST /api/v1/rooms
feat: implement GET /api/v1/rooms/{roomId}
feat: implement DELETE /api/v1/rooms/{roomId} with sensor guard logic
```

### Report Questions (Part 2)
> **Q2.1:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.
>
> **Q2.2:** Is the DELETE operation idempotent in your implementation? What happens if a client sends the exact same DELETE request for a room multiple times?

---

## ✅ Day 3 — April 19 (Sat): Sensor Management + Filtering
> **Part 3 — 20 Marks**

**Goal:** Sensor registration with room-link validation and optional type filtering.

### Tasks
- [ ] Create `SensorResource.java` registered at `@Path("/sensors")`
- [ ] `POST /api/v1/sensors`:
  - Check `roomId` in request body exists in DataStore
  - If not → throw `LinkedResourceNotFoundException` (wired up Day 4)
  - On success → save sensor AND add `sensorId` to `room.sensorIds`
  - Return `201 Created`
- [ ] `GET /api/v1/sensors` → return all sensors as JSON array
- [ ] `GET /api/v1/sensors?type=CO2` → use `@QueryParam("type")` to filter by type (case-insensitive recommended)
- [ ] `GET /api/v1/sensors/{sensorId}` → return specific sensor or `404`
- [ ] `DELETE /api/v1/sensors/{sensorId}`:
  - Remove sensor from DataStore AND remove its ID from parent room's `sensorIds`
  - Return `204 No Content`

### Commit Messages
```
feat: implement POST /api/v1/sensors with roomId validation
feat: implement GET /api/v1/sensors with optional ?type= query filter
feat: implement GET /api/v1/sensors/{sensorId}
feat: implement DELETE /api/v1/sensors/{sensorId} with room list cleanup
```

### Report Questions (Part 3)
> **Q3.1:** We use `@Consumes(MediaType.APPLICATION_JSON)` on the POST method. What are the technical consequences if a client sends `text/plain` or `application/xml`? How does JAX-RS handle the mismatch?
>
> **Q3.2:** The type filter uses `@QueryParam`. Contrast this with embedding type in the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching?

---

## ✅ Day 4 — April 20 (Sun): Sub-Resources + Error Handling + Logging
> **Part 4 — 20 Marks + Part 5 — 30 Marks**

**Goal:** Sensor readings sub-resource, all 5 exception mappers, and the logging filter.

### Sub-Resource Tasks (Part 4)
- [ ] In `SensorResource.java`, add sub-resource locator:
  ```java
  @Path("{sensorId}/readings")
  public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
      return new SensorReadingResource(sensorId);
  }
  ```
- [ ] Create `SensorReadingResource.java` (no `@Path` at class level):
  - `GET /` → return all readings for the sensor from DataStore
  - `POST /` → add a new reading:
    - Sensor not found → `404`
    - Sensor status is `"MAINTENANCE"` → throw `SensorUnavailableException`
    - Success → save reading, **update `sensor.currentValue`** to new value, return `201 Created`

### Exception Mapper Tasks (Part 5)

| Exception Class                   | Mapper Class                              | HTTP Code         | Trigger                              |
|-----------------------------------|-------------------------------------------|-------------------|--------------------------------------|
| `RoomNotEmptyException`           | `RoomNotEmptyExceptionMapper`             | 409 Conflict      | DELETE room that has sensors         |
| `LinkedResourceNotFoundException` | `LinkedResourceNotFoundExceptionMapper`   | 422 Unprocessable | POST sensor with non-existent roomId |
| `SensorUnavailableException`      | `SensorUnavailableExceptionMapper`        | 403 Forbidden     | POST reading to MAINTENANCE sensor   |
| `ResourceNotFoundException`       | `ResourceNotFoundExceptionMapper`         | 404 Not Found     | Any resource not found               |
| `Throwable` (catch-all)           | `GlobalExceptionMapper`                   | 500 Server Error  | Any unexpected runtime exception     |

- [ ] Each mapper must return a JSON body (not a stack trace) with a `message` field
- [ ] Create `ApiLoggingFilter.java` implementing both `ContainerRequestFilter` and `ContainerResponseFilter`:
  - Request: log HTTP method + request URI using `java.util.logging.Logger`
  - Response: log the final HTTP status code
  - Annotate with `@Provider`

### Commit Messages
```
feat: implement sub-resource locator for /sensors/{sensorId}/readings
feat: implement GET and POST for SensorReadingResource with currentValue side-effect
feat: add RoomNotEmptyException and 409 ExceptionMapper
feat: add LinkedResourceNotFoundException and 422 ExceptionMapper
feat: add SensorUnavailableException and 403 ExceptionMapper
feat: add ResourceNotFoundException and 404 ExceptionMapper
feat: add GlobalExceptionMapper as 500 catch-all safety net
feat: add ApiLoggingFilter for request and response observability
```

### Report Questions (Part 4 & 5)
> **Q4.1:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs?
>
> **Q5.2:** Why is HTTP 422 often more semantically accurate than 404 when the issue is a missing reference inside a valid JSON payload?
>
> **Q5.4:** From a cybersecurity standpoint, what are the risks of exposing internal Java stack traces to external API consumers?
>
> **Q5.5:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging rather than manually inserting `Logger.info()` in every resource method?

---

## ✅ Day 5 — April 21 (Mon): Deploy to Tomcat + Postman Testing + README + Video
> **All Parts**

**Goal:** WAR deployed to Tomcat, everything tested in Postman, README complete, video recorded.

### Tomcat Deployment Steps
```
1. mvn clean package
   → generates target/Smart-Campus-API.war

2. Copy WAR to:  <TOMCAT_HOME>/webapps/

3. Start Tomcat: <TOMCAT_HOME>/bin/startup.bat

4. Base URL in Postman:
   http://localhost:8080/Smart-Campus-API/api/v1
```

### Postman Testing Checklist
- [ ] `GET  /api/v1` — discovery endpoint
- [ ] `POST /api/v1/rooms` → `201 Created`
- [ ] `GET  /api/v1/rooms` → list of rooms
- [ ] `GET  /api/v1/rooms/{roomId}` → single room
- [ ] `DELETE /api/v1/rooms/{roomId}` — with sensors (→ 409) and without (→ 204)
- [ ] `POST /api/v1/sensors` — valid roomId (→ 201) and non-existent roomId (→ 422)
- [ ] `GET  /api/v1/sensors` → all sensors
- [ ] `GET  /api/v1/sensors?type=CO2` → filtered list
- [ ] `GET  /api/v1/sensors/{sensorId}` → single sensor
- [ ] `POST /api/v1/sensors/{id}/readings` — MAINTENANCE sensor (→ 403) and ACTIVE (→ 201)
- [ ] `GET  /api/v1/sensors/{id}/readings` → reading history
- [ ] Trigger a `500` by introducing a quick test to verify GlobalExceptionMapper

### README Tasks
- [ ] Write complete `README.md` containing:
  - [ ] API overview and resource hierarchy
  - [ ] Step-by-step build + Tomcat deploy instructions
  - [ ] At least **5 curl sample commands** with expected responses
  - [ ] All question answers (Parts 1–5)
- [ ] Record ≤10 min **Postman video demo**:
  - Camera + microphone must be active
  - Walk through each endpoint and all error scenarios
- [ ] Final review of all commits — ensure messages are clean
- [ ] Submit GitHub repo link + video on Blackboard

### Commit Messages
```
docs: write complete README with Tomcat deploy instructions and curl examples
docs: add all report question answers to README
chore: final cleanup and polish
```

---

## 🏗️ Project Package Structure

> ⚠️ **No `Main.java`** — Tomcat is the server. The WAR is dropped into Tomcat's `webapps/` folder.

```
Smart-Campus-API/
├── pom.xml                          ← packaging = war
├── README.md
├── PLAN.md                          ← you are here
└── src/
    └── main/
        ├── java/
        │   └── com/smartcampus/
        │       ├── SmartCampusApplication.java   ← @ApplicationPath("/api/v1")
        │       ├── data/
        │       │   └── DataStore.java            ← in-memory singleton
        │       ├── model/
        │       │   ├── Room.java
        │       │   ├── Sensor.java
        │       │   └── SensorReading.java
        │       ├── resource/
        │       │   ├── DiscoveryResource.java
        │       │   ├── RoomResource.java
        │       │   ├── SensorResource.java
        │       │   └── SensorReadingResource.java
        │       ├── exception/
        │       │   ├── RoomNotEmptyException.java
        │       │   ├── LinkedResourceNotFoundException.java
        │       │   ├── SensorUnavailableException.java
        │       │   └── ResourceNotFoundException.java
        │       ├── mapper/
        │       │   ├── RoomNotEmptyExceptionMapper.java
        │       │   ├── LinkedResourceNotFoundExceptionMapper.java
        │       │   ├── SensorUnavailableExceptionMapper.java
        │       │   ├── ResourceNotFoundExceptionMapper.java
        │       │   └── GlobalExceptionMapper.java
        │       └── filter/
        │           └── ApiLoggingFilter.java
        └── webapp/
            └── WEB-INF/
                └── web.xml                       ← minimal, triggers Servlet 3.0 scan
```

### 🔄 Build & Deploy Flow
```
mvn clean package
     ↓
target/Smart-Campus-API.war
     ↓
Copy to <TOMCAT_HOME>/webapps/
     ↓
Start Tomcat → startup.bat
     ↓
Postman: http://localhost:8080/Smart-Campus-API/api/v1
```

---

*Last updated: April 16, 2026 — Updated to use standalone Tomcat WAR deployment*
