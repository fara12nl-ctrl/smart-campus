# OOP + Java Concept Mapping (for the PBL report)

Every required concept from Units 1-6 and where it is implemented.

## 1. Classes, objects, constructors, methods, packages

- Classes: `Student`, `Faculty`, `Course`, `Classroom`, `Laboratory`, `Equipment`,
  `CampusEvent`, `Booking`, `UserAccount` in `backend/src/main/java/com/smartcampus/model/`.
- Constructors: overloaded no-arg + full constructors on every entity
  (e.g. `Student()` and `Student(long id, String rollNo, ...)`).
- Methods: getters/setters + behaviour methods (`isBookable`, `getRole`, `getType`).
- Packages: `model`, `dao`, `service`, `web`, `security`, `exception`, `dto`, `util`, `config`.

## 2. Encapsulation

- Fields are `private`/`protected`; access only through getters/setters.
- `AuthService` hides token storage (`ConcurrentHashMap`) behind login/verify methods.

## 3. Inheritance

```
CampusEntity (abstract)  ->  Person (abstract) -> Student, Faculty
                         ->  Course, CampusEvent, Booking
                         ->  Resource (abstract) -> Classroom, Laboratory
                         ->  Equipment
```
- `Student extends Person extends CampusEntity`
- `Laboratory extends Resource` reuses the base booking rule via `super.isBookable(...)`.

## 4. Polymorphism

- Method overriding: `getKind()` returns "STUDENT"/"FACULTY"/"LAB"/"EQUIPMENT"...
- `Person.getRole()` abstract - implemented differently per subclass.
- `Resource.isBookable()` base rule; `Laboratory` overrides it with a 4-hour cap;
  `Equipment` overrides to require status = AVAILABLE.
- `RoomRepository` returns `Classroom` or `Laboratory` as `Resource` based on the DB
  `type` column - runtime polymorphism.
- `BookingService.resolve()` treats `Resource` and `Equipment` through the
  `Bookable` interface.

## 5. Abstraction + Interfaces

- Abstract classes: `CampusEntity`, `Person`, `Resource`.
- Interfaces:
  - `Identifiable` (`long getId(); void setId(long);`)
  - `Bookable` (`boolean isBookable(LocalDateTime start, LocalDateTime end);`)
  - `CrudRepository<T>` (generic CRUD contract)
- Controllers depend on the `CrudRepository<T>` interface, not concrete repositories.

## 6. Generics

- `ApiResponse<T>` - one response envelope for every endpoint.
- `CrudRepository<T extends Identifiable>` + `AbstractJdbcRepository<T>` - one generic
  JDBC implementation shared by 7 repositories (table name, SQL, mapper supplied by
  subclasses).
- `AbstractCrudController<T extends Identifiable>` - one generic REST controller base
  shared by 7 controllers; subclasses only supply search text.

## 7. Exception handling

- Custom hierarchy: `ApiException` (base) -> `NotFoundException` (404),
  `BookingConflictException` (409), `ValidationException` (400).
- `GlobalExceptionHandler` (`@RestControllerAdvice`) converts exceptions into
  consistent JSON `{success, message, data}` with correct HTTP status.
- `try-with-resources` in `ApiClient` (Android) for streams.
- Android validates inputs and surfaces API error messages via Toast.

## 8. Multithreading + synchronization

- `BookingService.book()` is `synchronized`; overlap check happens inside the lock.
- `RaceDemoService` uses `ExecutorService`, `CountDownLatch` (start gate),
  `AtomicInteger`, `CopyOnWriteArrayList`; `GET /api/demo/race` lets N threads race
  for the same slot - exactly one succeeds, the rest get 409 conflicts.
- Android performs every network call on a background `Thread` and updates UI via
  `runOnUiThread`.

## 9. Collections

- `List<Student>` etc. returned by repositories.
- `LinkedHashMap` keeps dashboard stat order.
- `ConcurrentHashMap` stores auth sessions thread-safely.
- `CopyOnWriteArrayList` collects race winners across threads.

## 10. Lambda expressions + functional interfaces + Streams

- `DashboardService.stats()`:
  - `students.stream().filter(...).collect(groupingBy(counting()))`
  - department summary sorted with `Map.Entry.comparingByValue()`
  - equipment status counts, upcoming events sorted + limit(5)
- Search: `all.stream().filter(e -> searchText(e).contains(q)).collect(toList())`
  (backend) and `ListActivity.filter()` (Android).
- Spring `RowMapper` implemented as lambdas in every repository.
- `Executors` + `Runnable` lambdas in `RaceDemoService`.

## 11. File handling

- SQL/data files under `database/` (`schema.sql`, `seed.sql`) are imported via
  the MariaDB client; the backend reads configuration from
  `application.properties`.
- Android persists the session (token, user, role) via `SharedPreferences`
  (`Session.java`), i.e. on-device file-backed storage.

## 12. JDBC + CRUD

- `JdbcTemplate` + `PreparedStatementCreator` + `GeneratedKeyHolder` in
  `AbstractJdbcRepository.create()`.
- Parameterized queries everywhere (SQL-injection safe, prepared statements).
- Full CRUD on students, faculty, courses, rooms (classrooms/labs), equipment,
  events, bookings.
- `BookingRepository.hasOverlap()` uses a time-overlap SQL predicate:
  `start_time < ? AND end_time > ?` with `status='CONFIRMED'`.

## 13. Menu-driven interface

- Android app: sidebar-like dashboard with quick-access buttons per module.
- Web dashboard: left navigation with one section per module.
- Every screen: Add / Edit (tap item) / Delete (long-press or button) / Search.

## 14. Simultaneous booking handling

- Backend: synchronized service method + DB overlap check = second line of defence.
- UI: conflict (HTTP 409) surfaces as "Resource is already booked for this time window".
