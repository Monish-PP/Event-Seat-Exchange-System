-- Sample Seed Data for Event Seat Exchange

-- Students
INSERT IGNORE INTO STUDENTS (student_id, student_name, email, department, year_of_study) VALUES
(1, 'Rahul Sharma', 'rahul.sharma@college.edu', 'Computer Science & Engineering', 3),
(2, 'Priya Patel', 'priya.patel@college.edu', 'Information Technology', 2),
(3, 'Monish Kumar', 'monish.k@college.edu', 'Artificial Intelligence & DS', 3),
(4, 'Ananya Iyer', 'ananya.iyer@college.edu', 'Electronics & Communication', 4),
(5, 'Vikram Singh', 'vikram.singh@college.edu', 'Mechanical Engineering', 2),
(6, 'Sneha Reddy', 'sneha.reddy@college.edu', 'Computer Science & Engineering', 1);

-- Events
INSERT IGNORE INTO EVENTS (event_id, event_name, event_date, event_time, venue, total_seats, available_seats, registration_deadline) VALUES
(1, 'AI & Robotics Tech Summit 2026', '2026-11-15', '09:30:00', 'APJ Abdul Kalam Auditorium', 120, 118, '2026-11-14 18:00:00'),
(2, 'National Hackathon: CodeSprint', '2026-11-20', '10:00:00', 'Innovation Lab Complex', 60, 58, '2026-11-19 23:59:00'),
(3, 'Cloud & DevOps Masterclass', '2026-11-25', '14:00:00', 'Seminar Hall B, CSE Block', 80, 79, '2026-11-24 12:00:00'),
(4, 'Annual College Cultural Fiesta: TARANG', '2026-12-05', '17:00:00', 'Open Air Amphitheatre', 500, 498, '2026-12-04 12:00:00');

-- Initial Registrations
INSERT IGNORE INTO EVENT_REGISTRATIONS (registration_id, event_id, student_id, registration_time, seat_status) VALUES
(1, 1, 1, '2026-10-10 10:15:00', 'AVAILABLE_FOR_TRANSFER'),
(2, 1, 4, '2026-10-10 11:20:00', 'CONFIRMED'),
(3, 2, 2, '2026-10-11 09:00:00', 'AVAILABLE_FOR_TRANSFER'),
(4, 2, 5, '2026-10-11 14:45:00', 'CONFIRMED'),
(5, 3, 3, '2026-10-12 16:30:00', 'CONFIRMED'),
(6, 4, 1, '2026-10-13 12:00:00', 'TRANSFERRED'),
(7, 4, 3, '2026-10-13 15:30:00', 'CONFIRMED');

-- Transfer Requests
INSERT IGNORE INTO SEAT_TRANSFER_REQUESTS (transfer_id, registration_id, requested_by, request_time, transfer_status) VALUES
(1, 1, 2, '2026-10-12 11:00:00', 'PENDING'),
(2, 1, 3, '2026-10-12 13:45:00', 'PENDING'),
(3, 3, 4, '2026-10-12 18:20:00', 'PENDING'),
(4, 6, 3, '2026-10-13 14:00:00', 'APPROVED');

-- Transfer History
INSERT IGNORE INTO SEAT_TRANSFER_HISTORY (history_id, transfer_id, previous_student_id, new_student_id, transfer_time) VALUES
(1, 4, 1, 3, '2026-10-13 15:30:00');
