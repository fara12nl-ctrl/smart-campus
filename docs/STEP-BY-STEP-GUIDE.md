# Smart Campus App — Step-by-Step Build Guide (College Explanation)

This file explains **how the whole project was built, start to finish** — which
file was written in which step, and what order to follow when explaining it in
college. (Short version: DB design first → then backend bottom-up (model → DAO →
service → controller → security) → then the web dashboard → then the Android app →
then run/deploy.)

---

## 0. The project in one line

**"Smart Campus Integrated Management System"** — an app where an admin manages
students, faculty, courses, rooms/labs, equipment, events and **resource bookings**.
The booking module also has a **multithreading + synchronization** demo
(N threads try to book the same slot; only 1 wins).

```
Android app (Java)  ─┐
                     ├──HTTP/JSON──> Spring Boot backend ──JDBC──> MySQL (smart_campus)
Web dashboard (JS)  ─┘               (controllers → services → DAO)
```

Live demo setup (currently running):

| Item | Value |
|---|---|
| Cloud backend | `https://smart-campus-nf7e.onrender.com` (Render, Docker) |
| Cloud database | Aiven MySQL 8.4 (schema + seed imported) |
| Android app | `apk/SmartCampus-v1.3.apk` (cloud URL as default) |
| Logins | `admin/admin123`, `faculty1/faculty123`, `student1/student123` |

---

## 1. First step — Tools & environment (Day 0)

Installed (everything was set up by the agent, but you should know the names):

- **JDK 21** — for both the backend and Android (Java 21 language level).
- **Maven 3.9** — backend build (`backend/pom.xml`).
- **Gradle 8.13 + AGP 8.11** — Android build (`android/app/build.gradle`).
- **MariaDB 11.8 (port 3307)** — local dev database.
- **Android Studio + SDK 36 + emulator (Pixel 5, API 36)** — to run/test the app.
- **Git + GitHub + Render account + Aiven MySQL** — for cloud deploy.

Folder structure:

```
smart-campus/
├── backend/        # Spring Boot REST API (Java 21)
├── android/        # Android app (plain Java Activities)
├── database/       # schema.sql + seed.sql
├── apk/            # signed release APKs (v1.0 → v1.3)
├── docs/           # OOP mapping + this guide + screenshots
└── scripts/        # one-click start/tunnel/keep-awake .bat files
```

---

## 2. Database design — `database/schema.sql`

The **data model** was designed first, because the rest of the code is built on it.

8 tables were created (`DROP DATABASE IF EXISTS` included, so it can be re-imported
fresh any time):

| Table | What it stores | Important points |
|---|---|---|
| `users` | logins (admin/faculty/student) | `password_hash` = SHA-256 hex (64 chars), `role` ENUM |
| `students` | roll no, name, dept, year | `CHECK (year BETWEEN 1 AND 5)` |
| `faculty` | emp id, name, dept, designation | |
| `courses` | code, title, credits, semester | FK → `faculty.id` (ON DELETE SET NULL) |
| `rooms` | classrooms + labs in one table | `type ENUM('CLASSROOM','LAB')` |
| `equipment` | asset tag, status | FK → `rooms.id`, `status` ENUM |
| `events` | title, date, venue | index on `event_date` |
| `bookings` | ROOM/EQUIPMENT slot bookings | `CHECK (end_time > start_time)`, composite index |

Two design decisions to highlight in college:

1. **Classroom and Laboratory are not separate tables** — one `rooms` table + a `type`
   column. On the Java side, that `type` decides whether a `Classroom` or a
   `Laboratory` object is created (the hook for polymorphism).
2. **`bookings` uses resource_type + resource_id** (a polymorphic foreign key) — a single
   table books both ROOM and EQUIPMENT.

Seed data — `database/seed.sql`: 3 users, 4 students, 3 faculty, 4 courses,
4 rooms, 4 equipment, 3 events, 3 bookings. The SHA-256 hashes of the login
passwords are pre-computed in the seed (e.g. the hash of admin123), so login works
immediately.

---

## 3. Backend skeleton — `backend/`

**Step 3.1** — Spring Initializr-style structure: `pom.xml` with 3 dependencies:
`spring-boot-starter-web` (REST), `spring-boot-starter-jdbc` (JdbcTemplate),
`mysql-connector-j` (driver). Parent = Spring Boot **3.5.0**, Java **21**.

**Step 3.2** — `SmartCampusApplication.java` contains only `@SpringBootApplication` +
`main()` — the entry point.

**Step 3.3** — `application.properties` keeps **all config in env variables**:

```properties
server.port=${PORT:8080}
spring.datasource.url=${DB_URL:jdbc:mysql://127.0.0.1:3307/smart_campus?...}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.hikari.maximum-pool-size=${DB_POOL:5}
```

This one trick means **the same JAR runs on local MariaDB and on cloud MySQL** —
just change the env vars. On Render, `DB_URL`, `DB_USER`, `DB_PASSWORD` are set
from the dashboard. The password is never committed to the repo.

---

## 4. OOP model layer — `backend/.../model/`

This is the **heart** of the project (the PBL requires demonstrating OOP).
Written bottom-up:

**Step 4.1 — Interfaces (contracts first):**
- `Identifiable` → `long getId(); void setId(long);`
- `Bookable` → `boolean isBookable(LocalDateTime start, LocalDateTime end);`

**Step 4.2 — Abstract root class `CampusEntity`:**
`implements Identifiable`, `protected long id`, and `abstract String getKind();`
— every entity must return its own "kind" (the hook for overriding).

**Step 4.3 — Inheritance chain 1 (persons):**

```
CampusEntity (abstract)
├── Person (abstract)          → fields: name, email, phone
│   ├── Student                → rollNo, department, year   (getRole()="STUDENT")
│   └── Faculty                → empId, department, designation (getRole()="FACULTY")
```

**Step 4.4 — Inheritance chain 2 (resources + Bookable):**

```
CampusEntity
├── Resource (abstract, implements Bookable)
│   ├── Classroom   → isBookable = base rule
│   └── Laboratory  → isBookable = base rule + max 4 hours (reuses super.isBookable(...))
├── Equipment (implements Bookable directly)
│   └── isBookable = status must be "AVAILABLE" + time valid
```

**Step 4.5 — Remaining entities:** `Course`, `CampusEvent`, `Booking`, `UserAccount`
(`UserAccount` is a simple POJO because it goes straight from a DB row to JSON).

**How to present this in college (4 concepts are proven right here):**

| Concept | Where |
|---|---|
| Encapsulation | all fields are `private/protected`, accessed only via getters/setters |
| Inheritance | `Student extends Person extends CampusEntity`, `Laboratory extends Resource` |
| Polymorphism | `getRole()` / `getType()` / `isBookable()` — same signature, different behaviour; `BookingService` works through the `Bookable` interface |
| Abstraction | `abstract class` + interfaces (`Identifiable`, `Bookable`) |

Full mapping: `docs/OOP-CONCEPTS.md` (ready-made pointers for the report).

---

## 5. DAO layer (JDBC + Generics) — `backend/.../dao/`

This is where the **real benefit of generics** shows:

**Step 5.1** — `CrudRepository<T>` interface: `findAll, findById, create, update, delete`.

**Step 5.2** — `AbstractJdbcRepository<T extends Identifiable>` — the entire JDBC CRUD
logic is written **in one place**:

- Queries via `JdbcTemplate`, `RowMapper<T>` (lambda) turns a row into an object.
- `create()` uses `PreparedStatement` + `GeneratedKeyHolder` → the auto-increment id is returned.
- A subclass supplies only six small things: `table()`, `selectSql()`, `mapper()`,
  `insertSql()`, `insertParams()`, `updateSql()`, `updateParams()`.
- All queries are **parameterized** (`?` placeholders) → SQL-injection safe.

**Step 5.3** — 8 concrete repositories: `StudentRepository`, `FacultyRepository`,
`CourseRepository`, `RoomRepository`, `EquipmentRepository`, `EventRepository`,
`BookingRepository`, `UserRepository`.

Two special ones:

- `RoomRepository` — reads the DB `type` and returns a `Laboratory` or a `Classroom`
  while keeping the type as `Resource` → a live example of **runtime polymorphism**.
- `BookingRepository.hasOverlap()` — SQL time-overlap check:
  `start_time < ? AND end_time > ? AND status='CONFIRMED'`.

---

## 6. Service layer — `backend/.../service/`

Business logic lives in separate classes (clean architecture) before the controllers:

**Step 6.1 — `BookingService` (the star of the project):**
- `public synchronized long book(Booking b)` — **synchronized** means only one thread
  at a time can be inside the booking logic → a double-booking race is impossible.
- Inside: validation (type ROOM/EQUIPMENT, end > start, no past slot), then
  `Bookable.isBookable(...)` (polymorphism), then the DB overlap check (second
  safety net), then the insert.

**Step 6.2 — `RaceDemoService` (multithreading demo):**
Using `ExecutorService` + a `CountDownLatch` start gate, N threads (2–20) try to book
the same slot simultaneously; `AtomicInteger` counts successes/conflicts; the result
is `{threads, successes, conflicts, winners}`. Expected output:
**1 success, N-1 conflicts** — this is the proof that synchronization works.

**Step 6.3 — `DashboardService`:** all the counts + Streams:
`groupingBy` (students per department, equipment status), sorted upcoming events,
`LinkedHashMap` to keep the order.

**Step 6.4 — `AuthService`:** SHA-256 hash comparison + random token
(`UUID`), tokens kept in a `ConcurrentHashMap` with an 8-hour TTL.

---

## 7. REST controllers — `backend/.../web/`

**Step 7.1 — Generic base `AbstractCrudController<T extends Identifiable>`:**
list (with `?q=` search), get, create, update, delete — written **once** and extended
by 7 controllers. Each subclass only overrides `searchText()`. Example:

```java
@RestController @RequestMapping("/api/students")
public class StudentController extends AbstractCrudController<Student> { ... }
```

**Step 7.2 — Special controllers:**
- `AuthController` → `POST /api/auth/login`, `/logout`, `GET /me`
- `BookingController` → create goes through `BookingService`, plus
  `/api/bookings/{id}/cancel`, `/resource/{type}/{id}`
- `DashboardController` → `GET /api/dashboard`
- `DemoController` → `GET /api/demo/race?type=ROOM&id=1&threads=5`
- `HealthController` → `GET /api/health` (`{status:UP, database:CONNECTED}`)

**Step 7.3 — Response envelope `ApiResponse<T>`:** every response has the same shape —
`{success, message, data}`. This keeps front-end code simple.

**Step 7.4 — Exceptions:**
- Custom hierarchy: `ApiException` (base) → `NotFoundException(404)`,
  `ValidationException(400)`, `BookingConflictException(409)`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) converts all of them into JSON.

**Step 7.5 — Security wiring:**
- `AuthInterceptor` checks `Authorization: Bearer <token>`
  (or `X-Auth-Token`) on every `/api/**` request; no token → 401.
- `WebConfig` registers the interceptor (`/api/auth/login` and `/api/health` are
  exempt) and enables CORS (for the web dashboard/phone).

---

## 8. Web dashboard — `backend/src/main/resources/static/`

Spring Boot serves the static files: `index.html`, `style.css`, `app.js`.
Vanilla JS (`fetch` + Bearer token) provides login + CRUD for every module + search.
No build tool needed — it deploys along with the backend, so opening
`https://smart-campus-nf7e.onrender.com/` shows the dashboard.

---

## 9. Android app — `android/` (plain Java, no libraries)

**Step 9.1** — `LoginActivity` — username/password + a **Server URL field**
(default = cloud URL, editable), login button, error text, progress bar.

**Step 9.2** — `ApiClient` — an `HttpURLConnection` wrapper: GET/POST/PUT/DELETE,
JSON body, `Authorization: Bearer` header, 10s connect / 20s read timeout,
`ApiError` exception carrying the server's message.

**Step 9.3** — `Session` — token + displayName + role stored in
**SharedPreferences** (file-backed storage), plus the selected server URL,
`restore()` for auto-login, `clear()` for logout.

**Step 9.4** — `Entities` — **declarative config**: for every module (students, faculty,
courses, resources, equipment, events, bookings) a list of fields (key, label,
type: TEXT/NUMBER/DATETIME/SELECT, required). From this single config,
`ListActivity` + `FormActivity` work for all modules — adding a new field means
changing one line.

**Step 9.5** — `DashboardActivity` — fetches `/api/dashboard` and renders 8 stat cards
+ quick-access buttons (one per module) + a Thread Demo button.
The network call runs on a **background Thread**, and the UI is updated via
`runOnUiThread` (following the main-thread rule).

**Step 9.6** — `ListActivity` — fetch + search (client-side filter) + tap = edit +
Add button + delete.

**Step 9.7** — `FormActivity` — dynamic form built from the config, validation,
save (POST/PUT), API errors shown as Toasts.

**Step 9.8** — `DemoActivity` — the "Thread Demo" screen: thread count + start/end time,
calls `/api/demo/race` and shows the result (e.g. *5 threads → 1 success,
4 conflicts*) — the strongest screen to show in a viva.

**Step 9.9** — UI: dark-purple theme, all custom drawables (`bg_card.xml`,
`bg_input.xml` etc.) — no external libraries, which is why the APK is only ~42 KB.

---

## 10. Local run — `scripts/`

| Script | Purpose |
|---|---|
| `start-db.bat` | start MariaDB (port 3307) |
| `start-backend.bat` | start the backend JAR (port 8080) |
| `start-all.bat` | both of the above + a public tunnel, all at once |
| `start-public.bat` / `start-tunnel.bat` | Cloudflare quick tunnel (PC → internet URL) |
| `keep-cloud-awake.bat` | pings the cloud URL every few minutes (to avoid Render sleep) |

Test order: `curl http://localhost:8080/api/health` → open the dashboard in a browser →
log in with `admin/admin123`.

---

## 11. Cloud deploy (what was actually done)

**Step 11.1 — Dockerfile** (`backend/Dockerfile`): multi-stage —
stage 1 `maven:3.9-eclipse-temurin-21` runs `mvn package`, stage 2 ships only the JRE +
the JAR (`-Xmx360m`), `PORT=8080`.

**Step 11.2 — `render.yaml`**: Render Blueprint — `type: web`, `runtime: docker`,
free plan, health check `/api/health`, env vars `DB_URL/DB_USER/DB_PASSWORD`
(`sync: false` → secrets come from the dashboard, not the repo).

**Step 11.3 — Database**: a free MySQL 8.4 instance was created on Aiven (TLS required),
connected using the local MySQL client, and `schema.sql` + `seed.sql` were imported.
The DB URL needed `sslMode=REQUIRED`.

**Step 11.4 — Deploy**: repo pushed to GitHub → Render Blueprint created
(`smart-campus-pbl`) → first build took ~2 min → status **Live** →
`https://smart-campus-nf7e.onrender.com`.

**Step 11.5 — Verification (what we did)**: `/api/health` → `UP + DB CONNECTED`,
then `POST /api/auth/login` to get a token, then login from the app.

**Step 11.6 — Free tier caveat**: a Render free instance sleeps after 15 min of idle;
the first request then takes ~50 s (the app waits, it doesn't error out).
`scripts/keep-cloud-awake.bat` can be used to keep it warm.

---

## 12. APK build & release

**Step 12.1** — Debug build: `gradle assembleDebug` → `adb install` on the emulator.

**Step 12.2** — Signing: `android/smartcampus-release.jks` (alias `smartcampus`);
configured under `signingConfigs.release` in `build.gradle`, `minifyEnabled false`.

**Step 12.3** — Cloud default: `ApiClient.DEFAULT_BASE_URL` =
`https://smart-campus-nf7e.onrender.com` (since v1.3) — the app works directly
against the cloud on a real phone too, with no URL to paste. The Server URL field
is still editable (for a local backend/tunnel).

**Step 12.4** — Release build: `gradle assembleRelease` →
`apk/SmartCampus-v1.3.apk` → `adb install -r` → login → dashboard → students
list test (verified on the emulator).

---

## 13. Testing checklist (before the college demo)

1. `GET /api/health` → `UP + CONNECTED`
2. App login `admin/admin123` → dashboard (students=4, faculty=3, attendance=87%)
3. Students → Add → new student appears in the list → Edit → Delete
4. Bookings → create a booking → then book the **same slot** again → error
   *"Resource is already booked for this time window"* (409)
5. Thread Demo: 10 threads → **1 success, 9 conflicts** (the proof of `synchronized`)
6. Wrong password → 401 "Invalid username or password"
7. API call without a token → 401 (interceptor)

---

## 14. Explanation order for college (5–7 min)

1. **Problem** — campus management is spread across separate registers/spreadsheets;
   we built one integrated system (web + Android).
2. **Architecture diagram** — Android/Web → REST → Spring Boot → MySQL
   (draw the diagram above on the board).
3. **OOP tour** — the model class hierarchy tree (diagram in Section 4);
   talk about polymorphism via `Person.getRole()` and `Laboratory.isBookable()`.
4. **Generics** — `CrudRepository<T>` + `AbstractCrudController<T>`: "one piece of
   code, seven modules" — this is the strongest PBL point.
5. **Multithreading** — run the Thread Demo live (from the cloud URL): 10 threads →
   1 success + 9 conflicts → explain `synchronized`.
6. **Deploy** — "the app keeps working even with the PC off" — Render URL + Aiven DB.
7. **Live demo** — log in on the phone/emulator → CRUD → booking conflict.

### Expected viva questions (ready answers)

- **Q: Where is polymorphism?** → `getRole()` returns a different value in each subclass;
  `RoomRepository` creates a `Classroom`/`Laboratory` object from the DB `type`;
  `BookingService` handles both through the `Bookable` interface.
- **Q: Why `synchronized`?** → If two requests hit the same slot, another thread could slip
  in between the overlap check and the insert (race condition → double booking). The lock
  makes the whole check-and-insert atomic.
- **Q: Is there a DB-level guard too?** → Yes, `BookingRepository.hasOverlap()` in SQL —
  a second line of defence along with the lock.
- **Q: SQL injection?** → All queries use `?` placeholders (PreparedStatement).
- **Q: Password storage?** → SHA-256 hash (plain passwords are never stored in the database).
- **Q: Benefit of generics?** → The duplicate code of 7 repositories/controllers lives in
  2 base classes; you also get compile-time type safety.
- **Q: Exception handling?** → Custom hierarchy + `@RestControllerAdvice` gives
  consistent JSON + correct HTTP status (400/401/404/409/500).
- **Q: How does the app connect from a phone?** → HTTPS + Bearer token;
  the base URL is editable, and the default is the cloud URL.
- **Q: Issue with the free cloud?** → The Render free instance sleeps when idle
  (cold start ~50 s); the keep-awake script keeps it warm.

---

## 15. File map (what each file does)

```
backend/src/main/java/com/smartcampus/
├── SmartCampusApplication.java   # entry point
├── config/WebConfig.java         # interceptor + CORS
├── model/                        # OOP: interfaces, abstract classes, entities
├── dao/                          # JDBC + generics CRUD
├── service/                      # business logic (booking, dashboard, race, auth)
├── web/                          # REST controllers
├── security/                     # token auth (interceptor + service)
├── exception/                    # custom exceptions + global handler
├── dto/                          # ApiResponse<T>, LoginRequest, RaceResult
└── util/Dates.java               # ISO date-time parsing/formatting

android/app/src/main/java/com/smartcampus/app/
├── ApiClient.java    # HTTP client (HttpURLConnection)
├── Session.java      # SharedPreferences (token, user, server URL)
├── Entities.java     # declarative module config (list + form)
├── LoginActivity.java / DashboardActivity.java / ListActivity.java
├── FormActivity.java / DemoActivity.java
└── (res/layout + res/drawable — dark-purple UI, no libraries)

database/schema.sql, seed.sql     # DB design + demo data
backend/Dockerfile, render.yaml   # cloud deploy
scripts/*.bat                     # one-click local run + tunnel + keep-awake
```

Related docs: `docs/OOP-CONCEPTS.md` (unit-wise OOP mapping),
`README.md` (setup/usage/API table).
