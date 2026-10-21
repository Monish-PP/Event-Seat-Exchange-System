package EventSeatExchange.service;

import EventSeatExchange.dao.*;
import EventSeatExchange.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SeatExchangeService {
    private static final Logger log = LoggerFactory.getLogger(SeatExchangeService.class);

    private final DataSource dataSource;
    private final StudentDao studentDao;
    private final EventDao eventDao;
    private final RegistrationDao registrationDao;
    private final TransferRequestDao transferRequestDao;
    private final TransferHistoryDao transferHistoryDao;

    public SeatExchangeService(DataSource dataSource,
                               StudentDao studentDao,
                               EventDao eventDao,
                               RegistrationDao registrationDao,
                               TransferRequestDao transferRequestDao,
                               TransferHistoryDao transferHistoryDao) {
        this.dataSource = dataSource;
        this.studentDao = studentDao;
        this.eventDao = eventDao;
        this.registrationDao = registrationDao;
        this.transferRequestDao = transferRequestDao;
        this.transferHistoryDao = transferHistoryDao;
    }

    // ==========================================
    // 1. STUDENT REGISTRATION
    // ==========================================
    public Student registerStudent(Student student) throws SQLException {
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
        if (student.getEmail() == null || !student.getEmail().contains("@")) {
            throw new IllegalArgumentException("Valid email address is required.");
        }
        Optional<Student> existing = studentDao.findByEmail(student.getEmail().trim());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Student with email " + student.getEmail() + " already exists.");
        }
        studentDao.insert(student);
        return student;
    }

    public List<Student> getAllStudents() throws SQLException {
        return studentDao.findAll();
    }

    public Optional<Student> getStudentById(int studentId) throws SQLException {
        return studentDao.findById(studentId);
    }

    // ==========================================
    // 2. EVENT MANAGEMENT
    // ==========================================
    public Event createEvent(Event event) throws SQLException {
        if (event.getEventName() == null || event.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be empty.");
        }
        if (event.getEventDate() == null || event.getEventTime() == null) {
            throw new IllegalArgumentException("Event date and time are required.");
        }
        if (event.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Total seats must be greater than zero.");
        }
        if (event.getAvailableSeats() <= 0) {
            event.setAvailableSeats(event.getTotalSeats());
        }
        if (event.getRegistrationDeadline() == null) {
            // Default deadline: 2 hours before event
            event.setRegistrationDeadline(LocalDateTime.of(event.getEventDate(), event.getEventTime()).minusHours(2));
        }

        eventDao.insert(event);
        return event;
    }

    public List<Event> getAllEvents() throws SQLException {
        return eventDao.findAll();
    }

    public List<Event> getUpcomingEvents() throws SQLException {
        return eventDao.findAllUpcoming();
    }

    public Optional<Event> getEventById(int eventId) throws SQLException {
        return eventDao.findById(eventId);
    }

    // ==========================================
    // 3. DIRECT SEAT REGISTRATION
    // ==========================================
    public EventRegistration bookDirectSeat(int eventId, int studentId) throws SQLException {
        Event event = eventDao.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        if (!studentDao.findById(studentId).isPresent()) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }

        // Check deadline
        if (event.getRegistrationDeadline() != null && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new IllegalStateException("Registration deadline for this event has already passed.");
        }

        // Check if student already has a confirmed or listed seat
        Optional<EventRegistration> activeReg = registrationDao.findActiveRegistration(eventId, studentId);
        if (activeReg.isPresent()) {
            throw new IllegalStateException("Student already has an active registration for this event (Seat status: "
                    + activeReg.get().getSeatStatus() + ").");
        }

        // Atomically decrement seat and insert registration
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean decremented = eventDao.decrementAvailableSeats(conn, eventId);
                if (!decremented) {
                    throw new IllegalStateException("No seats available for this event.");
                }

                EventRegistration reg = new EventRegistration(eventId, studentId, "CONFIRMED");
                registrationDao.insert(conn, reg);

                conn.commit();
                log.info("Student {} successfully booked seat for event {}", studentId, eventId);
                return reg;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ==========================================
    // 4. LIST SEATS FOR TRANSFER
    // ==========================================
    public EventRegistration listSeatForTransfer(int registrationId, int studentId) throws SQLException {
        EventRegistration reg = registrationDao.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with ID: " + registrationId));

        if (reg.getStudentId() != studentId) {
            throw new IllegalArgumentException("Unauthorized: You do not own this seat registration.");
        }

        if (!"CONFIRMED".equalsIgnoreCase(reg.getSeatStatus())) {
            throw new IllegalStateException("Only CONFIRMED seats can be listed for transfer. Current status: " + reg.getSeatStatus());
        }

        if (reg.getEventDate() != null && reg.getEventDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalStateException("Cannot list seat for transfer: The event date has already passed.");
        }

        registrationDao.updateStatus(registrationId, "AVAILABLE_FOR_TRANSFER");
        reg.setSeatStatus("AVAILABLE_FOR_TRANSFER");
        log.info("Registration {} marked AVAILABLE_FOR_TRANSFER by student {}", registrationId, studentId);
        return reg;
    }

    // Cancel listing and revert to CONFIRMED
    public EventRegistration cancelSeatListing(int registrationId, int studentId) throws SQLException {
        EventRegistration reg = registrationDao.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with ID: " + registrationId));

        if (reg.getStudentId() != studentId) {
            throw new IllegalArgumentException("Unauthorized: You do not own this seat registration.");
        }

        if (!"AVAILABLE_FOR_TRANSFER".equalsIgnoreCase(reg.getSeatStatus())) {
            throw new IllegalStateException("Only seats listed as AVAILABLE_FOR_TRANSFER can be cancelled.");
        }

        registrationDao.updateStatus(registrationId, "CONFIRMED");
        reg.setSeatStatus("CONFIRMED");
        log.info("Registration {} reverted to CONFIRMED by student {}", registrationId, studentId);
        return reg;
    }

    // ==========================================
    // 5. REQUEST AVAILABLE SEATS
    // ==========================================
    public SeatTransferRequest requestSeatTransfer(int registrationId, int requesterStudentId) throws SQLException {
        EventRegistration reg = registrationDao.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with ID: " + registrationId));

        if (!"AVAILABLE_FOR_TRANSFER".equalsIgnoreCase(reg.getSeatStatus())) {
            throw new IllegalStateException("This seat is not available for transfer (Status: " + reg.getSeatStatus() + ").");
        }

        if (reg.getStudentId() == requesterStudentId) {
            throw new IllegalArgumentException("You cannot request your own listed seat.");
        }

        if (!studentDao.findById(requesterStudentId).isPresent()) {
            throw new IllegalArgumentException("Student not found with ID: " + requesterStudentId);
        }

        // Check if requester already has an active seat for this event
        Optional<EventRegistration> active = registrationDao.findActiveRegistration(reg.getEventId(), requesterStudentId);
        if (active.isPresent()) {
            throw new IllegalStateException("You already have an active seat for this event.");
        }

        // Check if student already submitted a pending request
        if (transferRequestDao.existsPendingRequest(registrationId, requesterStudentId)) {
            throw new IllegalStateException("You already have a pending transfer request for this seat.");
        }

        SeatTransferRequest req = new SeatTransferRequest(registrationId, requesterStudentId, "PENDING");
        transferRequestDao.insert(req);
        log.info("Student {} submitted transfer request for registration {}", requesterStudentId, registrationId);
        return req;
    }

    // ==========================================
    // 6. MANAGE TRANSFER REQUESTS
    // ==========================================
    public List<EventRegistration> getSeatsAvailableForTransfer() throws SQLException {
        return registrationDao.findAvailableForTransfer();
    }

    public List<EventRegistration> getRegistrationsByStudent(int studentId) throws SQLException {
        return registrationDao.findByStudentId(studentId);
    }

    public List<SeatTransferRequest> getIncomingTransferRequests(int ownerStudentId) throws SQLException {
        return transferRequestDao.findIncomingRequestsForOwner(ownerStudentId);
    }

    public List<SeatTransferRequest> getOutgoingTransferRequests(int requesterStudentId) throws SQLException {
        return transferRequestDao.findOutgoingRequestsByRequester(requesterStudentId);
    }

    public boolean rejectTransfer(int transferId, int ownerStudentId) throws SQLException {
        SeatTransferRequest req = transferRequestDao.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer request not found with ID: " + transferId));

        if (req.getCurrentOwnerId() != ownerStudentId) {
            throw new IllegalArgumentException("Unauthorized: You do not own the seat associated with this request.");
        }

        if (!"PENDING".equalsIgnoreCase(req.getTransferStatus())) {
            throw new IllegalStateException("Only PENDING requests can be rejected. Current status: " + req.getTransferStatus());
        }

        return transferRequestDao.updateStatus(transferId, "REJECTED");
    }

    public boolean cancelTransferRequest(int transferId, int requesterStudentId) throws SQLException {
        SeatTransferRequest req = transferRequestDao.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer request not found with ID: " + transferId));

        if (req.getRequestedBy() != requesterStudentId) {
            throw new IllegalArgumentException("Unauthorized: You can only cancel your own transfer request.");
        }

        if (!"PENDING".equalsIgnoreCase(req.getTransferStatus())) {
            throw new IllegalStateException("Only PENDING requests can be cancelled. Current status: " + req.getTransferStatus());
        }

        return transferRequestDao.updateStatus(transferId, "CANCELLED");
    }

    // ==========================================
    // 7. ATOMIC SEAT TRANSFER EXECUTION & CONCURRENCY CONTROL
    // ==========================================
    public Map<String, Object> approveTransfer(int transferId, int ownerStudentId) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Lock the transfer request row
                SeatTransferRequest req = transferRequestDao.findByIdForUpdate(conn, transferId)
                        .orElseThrow(() -> new IllegalArgumentException("Transfer request not found: " + transferId));

                if (!"PENDING".equalsIgnoreCase(req.getTransferStatus())) {
                    throw new IllegalStateException("Cannot approve request. Status is already: " + req.getTransferStatus());
                }

                // 2. Lock the target registration row
                EventRegistration originalReg = registrationDao.findByIdForUpdate(conn, req.getRegistrationId())
                        .orElseThrow(() -> new IllegalArgumentException("Associated registration not found: " + req.getRegistrationId()));

                if (originalReg.getStudentId() != ownerStudentId) {
                    throw new IllegalArgumentException("Unauthorized: You are not the owner of this registration.");
                }

                if (!"AVAILABLE_FOR_TRANSFER".equalsIgnoreCase(originalReg.getSeatStatus())) {
                    throw new IllegalStateException("Seat is no longer available for transfer! Status: " + originalReg.getSeatStatus());
                }

                int prevStudentId = originalReg.getStudentId();
                int newStudentId = req.getRequestedBy();
                int eventId = originalReg.getEventId();

                // 3. Update original registration status to TRANSFERRED
                registrationDao.updateStatus(conn, originalReg.getRegistrationId(), "TRANSFERRED");

                // 4. Create new registration for the new student with CONFIRMED status
                EventRegistration newReg = new EventRegistration(eventId, newStudentId, "CONFIRMED");
                int newRegId = registrationDao.insert(conn, newReg);

                // 5. Update this transfer request status to APPROVED
                transferRequestDao.updateStatus(conn, transferId, "APPROVED");

                // 6. Automatically reject any other competing pending requests for this seat
                int rejectedCount = transferRequestDao.rejectOtherPendingRequests(conn, originalReg.getRegistrationId(), transferId);
                log.info("Automatically rejected {} competing pending request(s) for registration {}", rejectedCount, originalReg.getRegistrationId());

                // 7. Record completed transfer in SEAT_TRANSFER_HISTORY
                SeatTransferHistory history = new SeatTransferHistory(transferId, prevStudentId, newStudentId);
                transferHistoryDao.insert(conn, history);

                conn.commit();
                log.info("Transfer {} successfully approved! Transferred from student {} to student {}", transferId, prevStudentId, newStudentId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("transferId", transferId);
                result.put("previousStudentId", prevStudentId);
                result.put("newStudentId", newStudentId);
                result.put("newRegistrationId", newRegId);
                result.put("rejectedOtherPendingCount", rejectedCount);
                return result;

            } catch (Exception ex) {
                conn.rollback();
                log.error("Transfer approval failed, transaction rolled back: {}", ex.getMessage());
                throw ex;
            }
        }
    }

    // ==========================================
    // 8. TRANSFER HISTORY & AUDIT
    // ==========================================
    public List<SeatTransferHistory> getTransferHistory() throws SQLException {
        return transferHistoryDao.findAll();
    }

    public List<SeatTransferHistory> getTransferHistoryForStudent(int studentId) throws SQLException {
        return transferHistoryDao.findByStudentId(studentId);
    }

    // Dashboard Statistics
    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        List<Event> events = eventDao.findAll();
        List<EventRegistration> exchangeSeats = registrationDao.findAvailableForTransfer();
        List<SeatTransferHistory> history = transferHistoryDao.findAll();
        List<Student> students = studentDao.findAll();

        int totalDirectSeatsAvailable = events.stream().mapToInt(Event::getAvailableSeats).sum();

        stats.put("totalEvents", events.size());
        stats.put("totalDirectAvailableSeats", totalDirectSeatsAvailable);
        stats.put("seatsListedForExchange", exchangeSeats.size());
        stats.put("completedTransfers", history.size());
        stats.put("totalStudents", students.size());
        return stats;
    }
}
