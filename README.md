# Smart Campus Integrated Management System

PBL project for **Object Oriented Programming with Practice (UE25CS2303)** - a Java/Spring Boot
REST backend with a MySQL/MariaDB database, a dark-purple web dashboard, and an Android (Java) app.

```
+---------------------+         HTTP/JSON          +---------------------------+
|  Android app (Java) |  ----------------------->  |  Spring Boot backend      |
|  login, dashboard,  |  smart-campus-nf7e         |  (Java 21, Render Docker) |
|  generic CRUD list  |  .onrender.com (default)   |  Controllers -> Services  |
+---------------------+                            |  -> DAO (JDBC) -> DB      |
                                                   +-------------+-------------+
+---------------------+         HTTP/JSON                        |
|  Web dashboard      |  ----------------------->                 v
|  (static HTML/JS)   |                             +--------------------------+
|  served at :8080/   |                             |  MariaDB / MySQL         |
+---------------------+                             |  schema: smart_campus     |
                                                    |  port: 3307              |
                                                    +--------------------------+
```

## Tech stack

| Layer      | Technology                                                     |
|------------|----------------------------------------------------------------|
| Backend    | Java 21, Spring Boot 3.5 (Web, JDBC), JdbcTemplate, KeyHolder   |
| Database   | MariaDB 11.8 / MySQL compatible, port 3307, database `smart_campus` |
| Frontend   | Vanilla HTML/CSS/JS served by Spring Boot static resources      |
| Mobile     | Android SDK 36, plain Java Activities (no external libraries)   |
| Build      | Maven 3.9 (backend), Gradle 8.13 + AGP 8.11 (Android)           |

## Folder structure

```
smart-campus/
├── database/
│   ├── schema.sql          # tables, constraints, indexes
│   └── seed.sql            # demo users + sample data
├── backend/
│   └── src/main/java/com/smartcampus/
│       ├── model/          # OOP entities (inheritance/polymorphism/interfaces)
│       ├── dao/            # generic JDBC repositories
│       ├── service/        # booking (synchronized), race demo, dashboard stats
│       ├── security/       # SHA-256 auth + token + interceptor
│       ├── web/            # REST controllers (generic CRUD base)
│       ├── exception/      # custom exceptions + global handler
│       ├── dto/            # ApiResponse<T>, requests, race result
│       └── util/           # date parsing helpers
│   └── src/main/resources/static/   # web dashboard (index.html, style.css, app.js)
├── android/                # Android app (Java, plain framework)
└── docs/OOP-CONCEPTS.md    # concept -> implementation mapping for the report
```

## How to run (this machine)

All tools are portable (no admin needed):

- JDK 21: `C:\Users\ABU TALHA\tools\jdk-21.0.13.1+2`
- Maven: `C:\Users\ABU TALHA\tools\apache-maven-3.9.16`
- Gradle: `C:\Users\ABU TALHA\tools\gradle-8.13`
- MariaDB: `C:\Users\ABU TALHA\tools\mariadb-11.8.10-winx64`

### 1. Start the database (port 3307)

```bat
scripts\start-db.bat
```

First time only - import schema + seed:

```bat
"C:\Users\ABU TALHA\tools\mariadb-11.8.10-winx64\bin\mariadb.exe" -uroot -h127.0.0.1 -P3307 ^
  < smart-campus\database\schema.sql
"C:\Users\ABU TALHA\tools\mariadb-11.8.10-winx64\bin\mariadb.exe" -uroot -h127.0.0.1 -P3307 ^
  < smart-campus\database\seed.sql
```

### 2. Start the backend (port 8080)

```bat
scripts\start-backend.bat
```

Health check: <http://localhost:8080/api/health>

### 3. Web dashboard

Open <http://localhost:8080/> and login with `admin` / `admin123`.
Features: dashboard stats, students, faculty, courses, classrooms & labs, equipment,
events, resource booking (with conflict detection) and the multithreading race demo.

### 4. Android app

```bat
cd smart-campus\android
C:\Users\ABU TALHA\tools\gradle-8.13\bin\gradle.bat assembleDebug
C:\Users\ABU TALHA\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
C:\Users\ABU TALHA\AppData\Local\Android\Sdk\platform-tools\adb.exe shell am start -n com.smartcampus.app/.LoginActivity
```

Or open `smart-campus/android` in Android Studio and press Run.
The app's default server is the hosted backend **https://smart-campus-nf7e.onrender.com**
(editable in the login screen's *Server URL* field). For a local backend instead,
enter `http://10.0.2.2:8080` (emulator loopback) in that field.

**Prebuilt APK:** `apk/SmartCampus-v1.3.apk` (signed release build, defaults to the cloud URL, tested on emulator).
Signing keystore: `android/smartcampus-release.jks` (store/key password: `smartcampus123`, alias `smartcampus`).
To rebuild the release APK: `gradle.bat assembleRelease` inside `smart-campus\android`.

### 5. Any network (mobile data / any Wi-Fi) — one-click public tunnel

`scripts\start-all.bat` starts everything in one go: MariaDB (if stopped),
backend (if stopped) and a Cloudflare quick tunnel with auto-restart.
The tunnel window shows a big **PUBLIC URL** (also saved to `tunnel-url.txt`).
Paste that URL into the app's **Server URL** field and sign in — works from
mobile data or any Wi-Fi, no account, no admin rights, no router changes.

Notes:
- Keep the PC on; close the tunnel window only when you are done.
- The quick-tunnel URL changes when the tunnel restarts — copy the new URL
  into the app (it is remembered after that). `start-public.bat` runs the
  tunnel alone if the rest is already running.
- Emulator default `http://10.0.2.2:8080` needs no tunnel.

### 6. Cloud deploy — LIVE (24/7 fixed URL, PC off bhi chalta hai)

**Live backend: <https://smart-campus-nf7e.onrender.com>**
(health: `/api/health`, web dashboard: `/` — login `admin` / `admin123`)

Deployed from this repo via Render Blueprint `smart-campus-pbl`
(`render.yaml` → Docker build of `backend/`). Database: hosted MySQL 8.4 on
Aiven (`smart_campus`, TLS required), schema + seed imported.

To redeploy/verify: push to GitHub `main` → Render auto-deploys from
`render.yaml`. Free tier sleeps after 15 min idle — the first request then
takes ~50 s (the app just waits; optionally run `scripts\keep-cloud-awake.bat`,
which reads `cloud-url.txt`). Aiven free trial credits expire ~30 days after
creation; after that either switch to another MySQL host or re-create it and
re-import `database/schema.sql` + `database/seed.sql`, then update the `DB_*`
env vars in the Render dashboard (secrets are NOT stored in this repo).

## Demo accounts

| Username | Password   | Role    |
|----------|------------|---------|
| admin    | admin123   | ADMIN   |
| faculty1 | faculty123 | FACULTY |
| student1 | student123 | STUDENT |

## REST API

| Method | Path                              | Purpose                          |
|--------|-----------------------------------|----------------------------------|
| POST   | /api/auth/login                   | Login, returns token             |
| POST   | /api/auth/logout                  | Invalidate token                 |
| GET    | /api/auth/me                      | Current user                     |
| GET    | /api/health                       | Status + DB connectivity         |
| CRUD   | /api/students[/{id}]              | Student management               |
| CRUD   | /api/faculty[/{id}]               | Faculty management               |
| CRUD   | /api/courses[/{id}]               | Course management                |
| CRUD   | /api/resources[/{id}]             | Classrooms + labs                |
| GET    | /api/resources/classrooms, /labs  | Filter by type                   |
| GET    | /api/resources/{id}/availability  | Availability + existing bookings |
| CRUD   | /api/equipment[/{id}]             | Equipment management             |
| CRUD   | /api/events[/{id}]                | Campus events                    |
| POST   | /api/bookings                     | Create booking (conflict-safe)   |
| PUT    | /api/bookings/{id}/cancel         | Cancel booking                   |
| GET    | /api/bookings/resource/{type}/{id}| Bookings for a resource          |
| GET    | /api/dashboard                    | Aggregated stats                 |
| GET    | /api/demo/race                    | N-thread booking race demo       |

All endpoints except `/api/auth/login` and `/api/health` require
`Authorization: Bearer <token>`.

`q` query parameter on list endpoints performs search (e.g. `/api/students?q=rahul`).

## Multithreading + synchronization

`BookingService.book()` is `synchronized`; before insert it checks
`bookings.hasOverlap(...)` inside the lock, so concurrent requests for the same slot
can never double-book. `GET /api/demo/race?type=ROOM&id=1&threads=5` fires N threads
at one slot and reports successes vs conflicts.

## More docs

- [docs/OOP-CONCEPTS.md](docs/OOP-CONCEPTS.md) - mapping of every course concept to code.
