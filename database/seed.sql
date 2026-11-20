-- ============================================================
-- Smart Campus - seed data
-- Logins:  admin/admin123, faculty1/faculty123, student1/student123
-- ============================================================
USE smart_campus;

INSERT INTO users (username, password_hash, role, display_name) VALUES
('admin',    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN',    'Campus Admin'),
('faculty1', '27041f5856c7387a997252694afb048d1aa939228ffcdbd6285b979b8da20e7a', 'FACULTY',  'Dr. Ayesha Khan'),
('student1', '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT',  'Aarav Sharma');

INSERT INTO students (roll_no, name, email, phone, department, year) VALUES
('CS24-001', 'Aarav Sharma',  'aarav.sharma@campus.edu',  '9876543210', 'Computer Science', 3),
('IT24-014', 'Diya Patel',    'diya.patel@campus.edu',    '9876543211', 'Information Tech', 3),
('EC24-102', 'Rohan Verma',   'rohan.verma@campus.edu',   '9876543212', 'Electronics',      2),
('CS24-057', 'Meera Nair',    'meera.nair@campus.edu',    '9876543213', 'Computer Science', 3);

INSERT INTO faculty (emp_id, name, email, phone, department, designation) VALUES
('FAC-CS-001', 'Dr. Ayesha Khan',  'ayesha.khan@campus.edu',  '9988776601', 'Computer Science', 'Associate Professor'),
('FAC-CS-002', 'Prof. Vikram Rao', 'vikram.rao@campus.edu',   '9988776602', 'Computer Science', 'Assistant Professor'),
('FAC-IT-003', 'Dr. Neha Gupta',   'neha.gupta@campus.edu',   '9988776603', 'Information Tech', 'Professor');

INSERT INTO courses (code, title, credits, semester, faculty_id) VALUES
('UE25CS2303', 'Object Oriented Programming', 4, 3, 1),
('CS301',      'Data Structures',             4, 3, 2),
('IT210',      'Database Management Systems', 3, 4, 3),
('CS305',      'Computer Networks',           3, 5, 2);

INSERT INTO rooms (room_no, type, capacity, building) VALUES
('A-101', 'CLASSROOM', 60, 'Academic Block A'),
('A-102', 'CLASSROOM', 45, 'Academic Block A'),
('LAB-1', 'LAB',       30, 'IT Block'),
('LAB-2', 'LAB',       30, 'IT Block');

INSERT INTO equipment (asset_tag, name, category, room_id, status) VALUES
('EQ-1001', 'Dell OptiPlex 7010',    'Computer',    3, 'AVAILABLE'),
('EQ-1002', 'Dell OptiPlex 7010',    'Computer',    3, 'AVAILABLE'),
('EQ-2001', 'Epson EB-X51 Projector','Projector',   1, 'AVAILABLE'),
('EQ-3001', 'Cisco 2911 Router',     'Networking',  4, 'MAINTENANCE');

INSERT INTO events (title, description, event_date, venue, organizer) VALUES
('TechFest 2026',   'Annual technology festival with hackathon and project expo', '2026-11-25 09:00:00', 'Main Auditorium', 'CS Department'),
('AI Workshop',     'Hands-on workshop on machine learning basics',                '2026-11-28 14:00:00', 'LAB-1',            'Dr. Ayesha Khan'),
('Campus Clean-Up', 'Volunteer drive for campus beautification',                   '2026-12-02 08:30:00', 'Academic Block A','Student Council');

INSERT INTO bookings (resource_type, resource_id, booked_by, purpose, start_time, end_time, status) VALUES
('ROOM',      1, 'Dr. Ayesha Khan', 'OOP extra class',            '2026-11-21 10:00:00', '2026-11-21 11:30:00', 'CONFIRMED'),
('ROOM',      3, 'Prof. Vikram Rao','DBMS practical',             '2026-11-21 14:00:00', '2026-11-21 16:00:00', 'CONFIRMED'),
('EQUIPMENT', 3, 'Dr. Neha Gupta',  'Project presentation setup', '2026-11-22 09:00:00', '2026-11-22 12:00:00', 'CONFIRMED');
