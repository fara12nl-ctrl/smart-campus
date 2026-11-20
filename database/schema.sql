-- ============================================================
-- Smart Campus Integrated Management System
-- MySQL schema (database: smart_campus)
-- ============================================================
DROP DATABASE IF EXISTS smart_campus;
CREATE DATABASE smart_campus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_campus;

-- ---------- application users (login) ----------
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL,              -- SHA-256 hex
    role          ENUM('ADMIN','FACULTY','STUDENT') NOT NULL DEFAULT 'ADMIN',
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------- students ----------
CREATE TABLE students (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    roll_no    VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(120) NOT NULL,
    phone      VARCHAR(20),
    department VARCHAR(60)  NOT NULL,
    year       INT          NOT NULL CHECK (year BETWEEN 1 AND 5)
);

-- ---------- faculty ----------
CREATE TABLE faculty (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    emp_id      VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(120) NOT NULL,
    phone       VARCHAR(20),
    department  VARCHAR(60)  NOT NULL,
    designation VARCHAR(60)  NOT NULL
);

-- ---------- courses ----------
CREATE TABLE courses (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(15)  NOT NULL UNIQUE,
    title      VARCHAR(120) NOT NULL,
    credits    INT          NOT NULL DEFAULT 3 CHECK (credits BETWEEN 1 AND 6),
    semester   INT          NOT NULL,
    faculty_id BIGINT,
    CONSTRAINT fk_course_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE SET NULL
);

-- ---------- rooms: classrooms & laboratories ----------
CREATE TABLE rooms (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_no  VARCHAR(15) NOT NULL UNIQUE,
    type     ENUM('CLASSROOM','LAB') NOT NULL,
    capacity INT NOT NULL,
    building VARCHAR(60) NOT NULL
);

-- ---------- equipment ----------
CREATE TABLE equipment (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_tag VARCHAR(20)  NOT NULL UNIQUE,
    name      VARCHAR(100) NOT NULL,
    category  VARCHAR(60)  NOT NULL,
    room_id   BIGINT,
    status    ENUM('AVAILABLE','IN_USE','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_equipment_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- ---------- campus events ----------
CREATE TABLE events (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    event_date  DATETIME     NOT NULL,
    venue       VARCHAR(100) NOT NULL,
    organizer   VARCHAR(100) NOT NULL
);

-- ---------- resource bookings ----------
CREATE TABLE bookings (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_type ENUM('ROOM','EQUIPMENT') NOT NULL,
    resource_id   BIGINT      NOT NULL,
    booked_by     VARCHAR(100) NOT NULL,
    purpose       VARCHAR(200) NOT NULL,
    start_time    DATETIME    NOT NULL,
    end_time      DATETIME    NOT NULL,
    status        ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_booking_time CHECK (end_time > start_time)
);

CREATE INDEX idx_booking_resource ON bookings(resource_type, resource_id, start_time, end_time);
CREATE INDEX idx_event_date ON events(event_date);
