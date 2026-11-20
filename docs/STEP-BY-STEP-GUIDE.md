# Smart Campus App — Step-by-Step Build Guide (College Explanation)

Ye file batati hai ki poora project **shuru se end tak kaise banaya gaya**, kaunsi
file kis step me likhi gayi, aur college me samjhate waqt kaunsa order follow karna hai.
(Short version: pehle DB design → phir backend bottom-up (model → DAO → service →
controller → security) → phir web dashboard → phir Android app → phir run/deploy.)

---

## 0. Project ek line me

**"Smart Campus Integrated Management System"** — ek app jisme admin students,
faculty, courses, rooms/labs, equipment, events aur **resource bookings**
manage kar sakta hai. Booking pe **multithreading + synchronization** ka demo
bhi hai (N threads ek hi slot book karne ki koshish karte hain, sirf 1 jeetta hai).

```
Android app (Java)  ─┐
                     ├──HTTP/JSON──> Spring Boot backend ──JDBC──> MySQL (smart_campus)
Web dashboard (JS)  ─┘               (controllers → services → DAO)
```

Live demo setup (jo abhi chal raha hai):

| Cheez | Value |
|---|---|
| Cloud backend | `https://smart-campus-nf7e.onrender.com` (Render, Docker) |
| Cloud database | Aiven MySQL 8.4 (schema + seed imported) |
| Android app | `apk/SmartCampus-v1.3.apk` (cloud URL default) |
| Logins | `admin/admin123`, `faculty1/faculty123`, `student1/student123` |

---

## 1. Pehla step — Tools & environment (Day 0)

Installed (sab agent ne setup kiya, but college me naam batana):

- **JDK 21** — backend aur Android dono ke liye (Java 21 language level).
- **Maven 3.9** — backend build (`backend/pom.xml`).
- **Gradle 8.13 + AGP 8.11** — Android build (`android/app/build.gradle`).
- **MariaDB 11.8 (port 3307)** — local dev database.
- **Android Studio + SDK 36 + emulator (Pixel 5, API 36)** — app run/test.
- **Git + GitHub + Render account + Aiven MySQL** — cloud deploy ke liye.

Folder structure:

```
smart-campus/
├── backend/        # Spring Boot REST API (Java 21)
├── android/        # Android app (plain Java Activities)
├── database/       # schema.sql + seed.sql
├── apk/            # signed release APKs (v1.0 → v1.3)
├── docs/           # OOP mapping + ye guide + screenshots
└── scripts/        # ek-click start/tunnel/keep-awake .bat files
```

---

## 2. Database design — `database/schema.sql`

Sabse pehle **data model** socha, kyunki baaki poora code isi pe based hai.

8 tables banaye (`DROP DATABASE IF EXISTS` ke saath, taaki baar-baar fresh import ho):

| Table | Kya rakhta hai | Important points |
|---|---|---|
| `users` | login (admin/faculty/student) | `password_hash` = SHA-256 hex (64 chars), `role` ENUM |
| `students` | roll no, name, dept, year | `CHECK (year BETWEEN 1 AND 5)` |
| `faculty` | emp id, name, dept, designation | |
| `courses` | code, title, credits, semester | FK → `faculty.id` (ON DELETE SET NULL) |
| `rooms` | classrooms + labs ek hi table me | `type ENUM('CLASSROOM','LAB')` |
| `equipment` | asset tag, status | FK → `rooms.id`, `status` ENUM |
| `events` | title, date, venue | index on `event_date` |
| `bookings` | ROOM/EQUIPMENT slot bookings | `CHECK (end_time > start_time)`, composite index |

Do design decisions jo college me highlight karne hain:

1. **Classroom aur Laboratory alag table nahi** — ek `rooms` table + `type` column.
   Java side pe isi `type` se `Classroom`/`Laboratory` object banta hai (polymorphism ka hook).
2. **`bookings` me resource_type + resource_id** (polymorphic foreign key) — ROOM ya
   EQUIPMENT dono ko ek hi table se book karte hain.

Seed data — `database/seed.sql`: 3 users, 4 students, 3 faculty, 4 courses,
4 rooms, 4 equipment, 3 events, 3 bookings. Login passwords ka SHA-256 hash seed me
pehle se hai (admin123 ka hash etc.), isliye login turant kaam karta hai.

---

## 3. Backend skeleton — `backend/`

**Step 3.1** — Spring Initializr jaisa structure: `pom.xml` me 3 dependencies:
`spring-boot-starter-web` (REST), `spring-boot-starter-jdbc` (JdbcTemplate),
`mysql-connector-j` (driver). Parent = Spring Boot **3.5.0**, Java **21**.

**Step 3.2** — `SmartCampusApplication.java` me sirf `@SpringBootApplication` +
`main()` — entry point.

**Step 3.3** — `application.properties` me **sab config env variables se**:

```properties
server.port=${PORT:8080}
spring.datasource.url=${DB_URL:jdbc:mysql://127.0.0.1:3307/smart_campus?...}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.hikari.maximum-pool-size=${DB_POOL:5}
```

Isi ek trick ki wajah se **same JAR local MariaDB pe bhi chalta hai aur cloud
MySQL pe bhi** — bas env vars badal do. Render pe `DB_URL`, `DB_USER`,
`DB_PASSWORD` dashboard se set kiye. Password repo me kabhi commit nahi hota.

---

## 4. OOP model layer — `backend/.../model/`

Ye project ka **heart** hai (PBL me OOP dikhana hai). Bottom-up order me likha:

**Step 4.1 — Interfaces (contracts pehle):**
- `Identifiable` → `long getId(); void setId(long);`
- `Bookable` → `boolean isBookable(LocalDateTime start, LocalDateTime end);`

**Step 4.2 — Abstract root class `CampusEntity`:**
`implements Identifiable`, `protected long id`, aur `abstract String getKind();`
— har entity ko apna "kind" return karna padega (overriding ka hook).

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
│   └── Laboratory  → isBookable = base rule + max 4 ghante (super.isBookable(...) reuse)
├── Equipment (implements Bookable directly)
│   └── isBookable = status "AVAILABLE" hona chahiye + time valid
```

**Step 4.5 — Baaki entities:** `Course`, `CampusEvent`, `Booking`, `UserAccount`
(`UserAccount` simple POJO hai kyunki wo DB row se seedha JSON jaata hai).

**College me kaise dikhayein (yahan se 4 concepts ka proof milta hai):**

| Concept | Kahan |
|---|---|
| Encapsulation | saare fields `private/protected`, access sirf getters/setters se |
| Inheritance | `Student extends Person extends CampusEntity`, `Laboratory extends Resource` |
| Polymorphism | `getRole()` / `getType()` / `isBookable()` — same signature, alag behaviour; `BookingService` `Bookable` interface se kaam karta hai |
| Abstraction | `abstract class` + interfaces (`Identifiable`, `Bookable`) |

Full mapping: `docs/OOP-CONCEPTS.md` (report ke liye ready-made pointers).

---

## 5. DAO layer (JDBC + Generics) — `backend/.../dao/`

Yahan **generics ka asli fayda** dikhta hai:

**Step 5.1** — `CrudRepository<T>` interface: `findAll, findById, create, update, delete`.

**Step 5.2** — `AbstractJdbcRepository<T extends Identifiable>` — poora JDBC CRUD
**ek hi jagah** likha:

- `JdbcTemplate` se query, `RowMapper<T>` (lambda) se row → object.
- `create()` me `PreparedStatement` + `GeneratedKeyHolder` → auto-increment id wapas.
- Subclass sirf 6 chhoti cheezein deta hai: `table()`, `selectSql()`, `mapper()`,
  `insertSql()`, `insertParams()`, `updateSql()`, `updateParams()`.
- Saare queries **parameterized** hain (`?` placeholders) → SQL injection safe.

**Step 5.3** — 8 concrete repositories: `StudentRepository`, `FacultyRepository`,
`CourseRepository`, `RoomRepository`, `EquipmentRepository`, `EventRepository`,
`BookingRepository`, `UserRepository`.

Do special wale:

- `RoomRepository` — DB ka `type` dekh ke `Laboratory` ya `Classroom` return karta
  hai, type `Resource` rakhte hue → **runtime polymorphism** ka live example.
- `BookingRepository.hasOverlap()` — SQL se time-overlap check:
  `start_time < ? AND end_time > ? AND status='CONFIRMED'`.

---

## 6. Service layer — `backend/.../service/`

Controller se pehle business logic alag class me (clean architecture):

**Step 6.1 — `BookingService` (project ka star):**
- `public synchronized long book(Booking b)` — **synchronized** matlab ek time pe
  ek hi thread booking logic ke andar → double-booking race impossible.
- Andar validation: type ROOM/EQUIPMENT, end > start, past slot nahi, phir
  `Bookable.isBookable(...)` (polymorphism), phir DB overlap check (second
  safety net), phir insert.

**Step 6.2 — `RaceDemoService` (multithreading demo):**
`ExecutorService` + `CountDownLatch` start-gate se N threads (2–20) ek saath
same slot book karne bhejta hai; `AtomicInteger` success/conflict count karta hai;
jekar result `{threads, successes, conflicts, winners}`. Expected output:
**1 success, N-1 conflicts** — yahi proof hai ki synchronization kaam kar raha hai.

**Step 6.3 — `DashboardService`:** saare counts + Streams:
`groupingBy` (department-wise students, equipment status), sorted upcoming events,
`LinkedHashMap` se order maintain.

**Step 6.4 — `AuthService`:** SHA-256 hash compare + random token
(`UUID`), tokens `ConcurrentHashMap` me 8 ghante TTL ke saath.

---

## 7. REST controllers — `backend/.../web/`

**Step 7.1 — Generic base `AbstractCrudController<T extends Identifiable>`:**
list (search `?q=` ke saath), get, create, update, delete — **ek hi baar** likha
gaya aur 7 controllers isse extend karte hain. Har subclass sirf `searchText()`
override karta hai. Example:

```java
@RestController @RequestMapping("/api/students")
public class StudentController extends AbstractCrudController<Student> { ... }
```

**Step 7.2 — Special controllers:**
- `AuthController` → `POST /api/auth/login`, `/logout`, `GET /me`
- `BookingController` → booking create `BookingService` se hota hai, plus
  `/api/bookings/{id}/cancel`, `/resource/{type}/{id}`
- `DashboardController` → `GET /api/dashboard`
- `DemoController` → `GET /api/demo/race?type=ROOM&id=1&threads=5`
- `HealthController` → `GET /api/health` (`{status:UP, database:CONNECTED}`)

**Step 7.3 — Response envelope `ApiResponse<T>`:** har response ka shape same —
`{success, message, data}`. Front-end code simple ho jaata hai.

**Step 7.4 — Exceptions:**
- Custom hierarchy: `ApiException` (base) → `NotFoundException(404)`,
  `ValidationException(400)`, `BookingConflictException(409)`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) sabko JSON me convert karta hai.

**Step 7.5 — Security wiring:**
- `AuthInterceptor` har `/api/**` request pe `Authorization: Bearer <token>`
  (ya `X-Auth-Token`) check karta hai, bina token 401.
- `WebConfig` interceptor register karta hai (`/api/auth/login` aur `/api/health`
  free) aur CORS allow karta hai (web dashboard/phone ke liye).

---

## 8. Web dashboard — `backend/src/main/resources/static/`

Spring Boot static files serve karta hai: `index.html`, `style.css`, `app.js`.
Vanilla JS (`fetch` + Bearer token) se login + har module ka CRUD + search.
Build tool nahi chahiye — backend ke saath hi deploy hota hai, isliye
`https://smart-campus-nf7e.onrender.com/` kholte hi dashboard milta hai.

---

## 9. Android app — `android/` (plain Java, no libraries)

**Step 9.1** — `LoginActivity` — username/password + **Server URL field**
(default = cloud URL, editable), login button, error text, progress bar.

**Step 9.2** — `ApiClient` — `HttpURLConnection` wrapper: GET/POST/PUT/DELETE,
JSON body, `Authorization: Bearer` header, 10s connect / 20s read timeout,
`ApiError` exception jisme server ka message hota hai.

**Step 9.3** — `Session` — token + displayName + role **SharedPreferences** me
(file-backed storage), plus selected server URL remember karna, `restore()` se
auto-login, `clear()` se logout.

**Step 9.4** — `Entities` — **declarative config**: har module (students, faculty,
courses, resources, equipment, events, bookings) ke fields ki list (key, label,
type: TEXT/NUMBER/DATETIME/SELECT, required). Isi ek config se
`ListActivity` + `FormActivity` sab modules ke liye kaam karte hain — naya field
add karna = ek line badalna.

**Step 9.5** — `DashboardActivity` — `/api/dashboard` se stats laa ke 8 cards
render karta hai + quick-access buttons (har module ke liye) + Thread Demo button.
Network call **background Thread** me, UI update `runOnUiThread` se (main-thread
rule follow).

**Step 9.6** — `ListActivity` — fetch + search (client-side filter) + tap = edit +
Add button + delete.

**Step 9.7** — `FormActivity` — config se dynamic form, validation, save (POST/PUT),
API error Toast.

**Step 9.8** — `DemoActivity` — "Thread Demo" screen: threads count + start/end time,
`/api/demo/race` call karke result dikhata hai (e.g. *5 threads → 1 success,
4 conflicts*) — viva me ye screen dikhana sabse strong demo hai.

**Step 9.9** — UI: dark-purple theme, sab custom drawables (`bg_card.xml`,
`bg_input.xml` etc.) — koi external library nahi, isliye APK sirf ~42 KB ka hai.

---

## 10. Local run — `scripts/`

| Script | Kaam |
|---|---|
| `start-db.bat` | MariaDB (port 3307) start |
| `start-backend.bat` | backend JAR (port 8080) start |
| `start-all.bat` | upar dono + public tunnel ek saath |
| `start-public.bat` / `start-tunnel.bat` | Cloudflare quick tunnel (PC → internet URL) |
| `keep-cloud-awake.bat` | har kuch minute me cloud URL ping (Render sleep se bachne ke liye) |

Test order: `curl http://localhost:8080/api/health` → browser me dashboard →
login `admin/admin123`.

---

## 11. Cloud deploy (jo actually kiya gaya)

**Step 11.1 — Dockerfile** (`backend/Dockerfile`): multi-stage —
stage 1 `maven:3.9-eclipse-temurin-21` me `mvn package`, stage 2 sirf JRE +
JAR (`-Xmx360m`), `PORT=8080`.

**Step 11.2 — `render.yaml`**: Render Blueprint — `type: web`, `runtime: docker`,
free plan, health check `/api/health`, env vars `DB_URL/DB_USER/DB_PASSWORD`
(`sync: false` → secrets dashboard se, repo me nahi).

**Step 11.3 — Database**: Aiven pe free MySQL 8.4 banaya (TLS required),
local MySQL client se connect karke `schema.sql` + `seed.sql` import kiye.
DB URL me `sslMode=REQUIRED` lagana zaroori tha.

**Step 11.4 — Deploy**: repo GitHub pe push → Render me Blueprint create
(`smart-campus-pbl`) → pehli build ~2 min → status **Live** →
`https://smart-campus-nf7e.onrender.com`.

**Step 11.5 — Verify (jo humne kiya)**: `/api/health` → `UP + DB CONNECTED`,
phir `POST /api/auth/login` se token, phir app se login.

**Step 11.6 — Free tier caveat**: Render free instance 15 min idle ke baad so
jaata hai; pehla request ~50 s leta hai (app wait karta hai, error nahi deta).
`scripts/keep-cloud-awake.bat` se warm rakha ja sakta hai.

---

## 12. APK build & release

**Step 12.1** — Debug build: `gradle assembleDebug` → emulator pe `adb install`.

**Step 12.2** — Signing: `android/smartcampus-release.jks` (alias `smartcampus`);
`build.gradle` ke `signingConfigs.release` me configured, `minifyEnabled false`.

**Step 12.3** — Cloud default: `ApiClient.DEFAULT_BASE_URL` =
`https://smart-campus-nf7e.onrender.com` (v1.3 se) — real phone pe bhi app
seedha cloud se chalti hai, koi URL paste karne ki zaroorat nahi.
Server URL field phir bhi editable hai (local backend/tunnel ke liye).

**Step 12.4** — Release build: `gradle assembleRelease` →
`apk/SmartCampus-v1.3.apk` → `adb install -r` → login → dashboard → students
list test (emulator pe verified).

---

## 13. Testing checklist (college demo se pehle)

1. `GET /api/health` → `UP + CONNECTED`
2. App login `admin/admin123` → dashboard (students=4, faculty=3, attendance=87%)
3. Students → Add → list me naya student → Edit → Delete
4. Bookings → naya booking → phir **same slot** dobara book → error
   *"Resource is already booked for this time window"* (409)
5. Thread Demo: 10 threads → **1 success, 9 conflicts** (synchronized ka proof)
6. Wrong password → 401 "Invalid username or password"
7. Bina token API call → 401 (interceptor)

---

## 14. College me explain karne ka order (5–7 min)

1. **Problem** — campus management alag-alag registers/spreadsheets me hota hai;
   humne ek integrated system banaya (web + Android).
2. **Architecture diagram** — Android/Web → REST → Spring Boot → MySQL
   (upar wala diagram board pe banao).
3. **OOP tour** — model classes ka hierarchy tree (Section 4 ka diagram),
   `Person.getRole()` aur `Laboratory.isBookable()` pe polymorphism bolo.
4. **Generics** — `CrudRepository<T>` + `AbstractCrudController<T>`: "ek code,
   saath modules" — ye PBL ka strong point hai.
5. **Multithreading** — Thread Demo live chalao (cloud URL se): 10 threads →
   1 success + 9 conflicts → `synchronized` explain karo.
6. **Deploy** — "PC band hone pe bhi app chalti hai" — Render URL + Aiven DB.
7. **Live demo** — phone/emulator pe login → CRUD → booking conflict.

### Viva ke expected sawaal (ready answers)

- **Q: Polymorphism kahan hai?** → `getRole()` har subclass me alag returns;
  `RoomRepository` DB `type` se `Classroom`/`Laboratory` object banata hai;
  `BookingService` `Bookable` interface se dono ko same tarike se handle karta hai.
- **Q: `synchronized` kyun lagaya?** → Do requests same slot pe aayein to
  overlap-check aur insert ke beech doosra thread ghus sakta hai (race condition
  → double booking). Lock se poora check-and-insert atomic ban jaata hai.
- **Q: DB me bhi guard hai?** → Haan, `BookingRepository.hasOverlap()` SQL —
  lock ke saath second line of defence.
- **Q: SQL injection?** → Saare queries `?` placeholders (PreparedStatement).
- **Q: Password storage?** → SHA-256 hash (database me plain password kabhi nahi).
- **Q: Generics ka fayda?** → 7 repositories/controllers ka duplicate code 2 base
  classes me hai; compile-time type safety bhi milti hai.
- **Q: Exception handling?** → Custom hierarchy + `@RestControllerAdvice` se
  consistent JSON + sahi HTTP status (400/401/404/409/500).
- **Q: App phone se kaise connect hoti hai?** → HTTPS + Bearer token;
  base URL editable hai, default cloud URL hai.
- **Q: Free cloud ka issue?** → Render free instance idle pe sleep karta hai
  (cold start ~50 s); keep-awake script se warm rakhte hain.

---

## 15. File map (kaunsi file kya karti hai)

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
└── (res/layout + res/drawable — dark-purple UI, koi library nahi)

database/schema.sql, seed.sql     # DB design + demo data
backend/Dockerfile, render.yaml   # cloud deploy
scripts/*.bat                     # ek-click local run + tunnel + keep-awake
```

Related docs: `docs/OOP-CONCEPTS.md` (unit-wise OOP mapping),
`README.md` (setup/usage/API table).
