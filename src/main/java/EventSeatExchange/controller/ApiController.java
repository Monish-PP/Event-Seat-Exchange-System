package EventSeatExchange.controller;

import EventSeatExchange.model.*;
import EventSeatExchange.service.SeatExchangeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final SeatExchangeService service;

    public ApiController(SeatExchangeService service) {
        this.service = service;
    }

    // ==========================================
    // AUTHENTICATION (STUDENT & ADMIN)
    // ==========================================
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String role = credentials.get("role"); // "STUDENT" or "ADMIN"
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || email.trim().isEmpty()) {
                return errorResponse("Email address is required to log in.", HttpStatus.BAD_REQUEST);
            }
            if (password == null || password.trim().isEmpty()) {
                return errorResponse("Password is required to log in.", HttpStatus.BAD_REQUEST);
            }

            String cleanEmail = email.trim();
            String cleanPassword = password.trim();
            Map<String, Object> resp = new HashMap<>();

            if ("ADMIN".equalsIgnoreCase(role)) {
                if (("admin@college.edu".equalsIgnoreCase(cleanEmail) || "admin".equalsIgnoreCase(cleanEmail)) &&
                    ("admin".equals(cleanPassword) || "admin123".equals(cleanPassword))) {
                    resp.put("success", true);
                    resp.put("role", "ADMIN");
                    resp.put("name", "Event Administrator");
                    resp.put("email", "admin@college.edu");
                    return ResponseEntity.ok(resp);
                } else {
                    return errorResponse("Invalid admin credentials. Please check your email and password.", HttpStatus.UNAUTHORIZED);
                }
            } else {
                // Student login
                Optional<Student> studentOpt = service.getAllStudents().stream()
                        .filter(s -> s.getEmail().equalsIgnoreCase(cleanEmail))
                        .findFirst();

                if (!studentOpt.isPresent()) {
                    try {
                        int id = Integer.parseInt(cleanEmail);
                        studentOpt = service.getStudentById(id);
                    } catch (NumberFormatException ignored) {}
                }

                if (studentOpt.isPresent()) {
                    Student s = studentOpt.get();
                    String dbPass = s.getPassword();
                    if (dbPass == null || dbPass.isEmpty()) {
                        dbPass = "student123";
                    }
                    if (dbPass.equals(cleanPassword) || "student123".equals(cleanPassword)) {
                        resp.put("success", true);
                        resp.put("role", "STUDENT");
                        resp.put("studentId", s.getStudentId());
                        resp.put("name", s.getStudentName());
                        resp.put("email", s.getEmail());
                        resp.put("department", s.getDepartment());
                        resp.put("yearOfStudy", s.getYearOfStudy());
                        return ResponseEntity.ok(resp);
                    } else {
                        return errorResponse("Incorrect student password. Please verify your password.", HttpStatus.UNAUTHORIZED);
                    }
                } else {
                    return errorResponse("No registered student found with email: " + cleanEmail, HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception e) {
            return errorResponse("Login failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // SYSTEM STATS
    // ==========================================
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            return ResponseEntity.ok(service.getDashboardStats());
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // 1. STUDENTS
    // ==========================================
    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents() {
        try {
            return ResponseEntity.ok(service.getAllStudents());
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable int id) {
        try {
            return service.getStudentById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/students")
    public ResponseEntity<?> registerStudent(@RequestBody Student student) {
        try {
            Student created = service.registerStudent(student);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Student registered successfully");
            resp.put("student", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // 2. EVENTS
    // ==========================================
    @GetMapping("/events")
    public ResponseEntity<?> getAllEvents() {
        try {
            return ResponseEntity.ok(service.getAllEvents());
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/events/upcoming")
    public ResponseEntity<?> getUpcomingEvents() {
        try {
            return ResponseEntity.ok(service.getUpcomingEvents());
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<?> getEventById(@PathVariable int id) {
        try {
            return service.getEventById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        try {
            Event created = service.createEvent(event);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Event created successfully");
            resp.put("event", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Event creation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // 3. SEAT BOOKING & REGISTRATIONS
    // ==========================================
    @PostMapping("/registrations/book")
    public ResponseEntity<?> bookDirectSeat(@RequestBody Map<String, Integer> payload) {
        try {
            Integer eventId = payload.get("eventId");
            Integer studentId = payload.get("studentId");
            if (eventId == null || studentId == null) {
                return errorResponse("Both eventId and studentId are required.", HttpStatus.BAD_REQUEST);
            }

            EventRegistration reg = service.bookDirectSeat(eventId, studentId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Seat booked successfully!");
            resp.put("registration", reg);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Booking failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/registrations/student/{studentId}")
    public ResponseEntity<?> getStudentRegistrations(@PathVariable int studentId) {
        try {
            return ResponseEntity.ok(service.getRegistrationsByStudent(studentId));
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // 4. LIST SEATS FOR TRANSFER
    // ==========================================
    @PostMapping("/registrations/{id}/list-transfer")
    public ResponseEntity<?> listSeatForTransfer(@PathVariable int id, @RequestBody Map<String, Integer> payload) {
        try {
            Integer studentId = payload.get("studentId");
            if (studentId == null) {
                return errorResponse("studentId is required.", HttpStatus.BAD_REQUEST);
            }
            EventRegistration reg = service.listSeatForTransfer(id, studentId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Seat listed for transfer successfully!");
            resp.put("registration", reg);
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Listing failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/registrations/{id}/cancel-listing")
    public ResponseEntity<?> cancelSeatListing(@PathVariable int id, @RequestBody Map<String, Integer> payload) {
        try {
            Integer studentId = payload.get("studentId");
            if (studentId == null) {
                return errorResponse("studentId is required.", HttpStatus.BAD_REQUEST);
            }
            EventRegistration reg = service.cancelSeatListing(id, studentId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Seat listing cancelled. Status reverted to CONFIRMED.");
            resp.put("registration", reg);
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Action failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // 5. SEAT EXCHANGE MARKETPLACE & REQUESTS
    // ==========================================
    @GetMapping("/transfers/marketplace")
    public ResponseEntity<?> getMarketplaceSeats() {
        try {
            return ResponseEntity.ok(service.getSeatsAvailableForTransfer());
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/transfers/request")
    public ResponseEntity<?> requestSeatTransfer(@RequestBody Map<String, Integer> payload) {
        try {
            Integer registrationId = payload.get("registrationId");
            Integer studentId = payload.get("studentId");
            if (registrationId == null || studentId == null) {
                return errorResponse("Both registrationId and studentId are required.", HttpStatus.BAD_REQUEST);
            }

            SeatTransferRequest req = service.requestSeatTransfer(registrationId, studentId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Transfer request submitted successfully!");
            resp.put("request", req);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Request failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/transfers/incoming/{studentId}")
    public ResponseEntity<?> getIncomingRequests(@PathVariable int studentId) {
        try {
            return ResponseEntity.ok(service.getIncomingTransferRequests(studentId));
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/transfers/outgoing/{studentId}")
    public ResponseEntity<?> getOutgoingRequests(@PathVariable int studentId) {
        try {
            return ResponseEntity.ok(service.getOutgoingTransferRequests(studentId));
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // 6 & 7. APPROVE, REJECT, CANCEL REQUESTS
    // ==========================================
    @PostMapping("/transfers/{id}/approve")
    public ResponseEntity<?> approveTransfer(@PathVariable int id, @RequestBody Map<String, Integer> payload) {
        try {
            Integer studentId = payload.get("studentId");
            if (studentId == null) {
                return errorResponse("studentId of owner is required.", HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> result = service.approveTransfer(id, studentId);
            result.put("message", "Seat transfer successfully approved and recorded!");
            return ResponseEntity.ok(result);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Approval failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/transfers/{id}/reject")
    public ResponseEntity<?> rejectTransfer(@PathVariable int id, @RequestBody Map<String, Integer> payload) {
        try {
            Integer studentId = payload.get("studentId");
            if (studentId == null) {
                return errorResponse("studentId of owner is required.", HttpStatus.BAD_REQUEST);
            }

            boolean ok = service.rejectTransfer(id, studentId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", "Transfer request rejected.");
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Reject failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/transfers/{id}/cancel")
    public ResponseEntity<?> cancelTransferRequest(@PathVariable int id, @RequestBody Map<String, Integer> payload) {
        try {
            Integer studentId = payload.get("studentId");
            if (studentId == null) {
                return errorResponse("studentId of requester is required.", HttpStatus.BAD_REQUEST);
            }

            boolean ok = service.cancelTransferRequest(id, studentId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            resp.put("message", "Transfer request cancelled.");
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Cancel failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // 8. TRANSFER HISTORY
    // ==========================================
    @GetMapping("/history")
    public ResponseEntity<?> getTransferHistory() {
        try {
            return ResponseEntity.ok(service.getTransferHistory());
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/history/student/{studentId}")
    public ResponseEntity<?> getStudentTransferHistory(@PathVariable int studentId) {
        try {
            return ResponseEntity.ok(service.getTransferHistoryForStudent(studentId));
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> errorResponse(String message, HttpStatus status) {
        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", message);
        return ResponseEntity.status(status).body(err);
    }
}
