package EventSeatExchange;

import EventSeatExchange.dao.EventDao;
import EventSeatExchange.dao.RegistrationDao;
import EventSeatExchange.dao.StudentDao;
import EventSeatExchange.dao.TransferHistoryDao;
import EventSeatExchange.dao.TransferRequestDao;
import EventSeatExchange.model.Event;
import EventSeatExchange.model.EventRegistration;
import EventSeatExchange.model.SeatTransferHistory;
import EventSeatExchange.model.SeatTransferRequest;
import EventSeatExchange.model.Student;
import EventSeatExchange.service.SeatExchangeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SeatExchangeServiceTest {

    @Autowired
    private SeatExchangeService service;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private RegistrationDao registrationDao;

    @Autowired
    private TransferRequestDao transferRequestDao;

    @Autowired
    private TransferHistoryDao transferHistoryDao;

    @Test
    @DisplayName("Functionality 1: Student Registration and duplicate prevention")
    void testStudentRegistration() throws Exception {
        String uniqueEmail = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@college.edu";
        Student s = new Student("Test Student", uniqueEmail, "Computer Science", 3);
        Student created = service.registerStudent(s);

        assertNotNull(created);
        assertTrue(created.getStudentId() > 0);

        // Attempt duplicate registration with same email
        Student duplicate = new Student("Duplicate Student", uniqueEmail, "IT", 2);
        assertThrows(IllegalArgumentException.class, () -> service.registerStudent(duplicate));
    }

    @Test
    @DisplayName("Functionality 2 & 3: Event creation and direct seat booking")
    void testEventCreationAndDirectBooking() throws Exception {
        // Create event
        String eventName = "HackFest_" + UUID.randomUUID().toString().substring(0, 6);
        Event ev = new Event(
                eventName,
                LocalDate.now().plusDays(10),
                LocalTime.of(10, 0),
                "Auditorium 1",
                50,
                50,
                LocalDateTime.now().plusDays(9)
        );
        Event createdEvent = service.createEvent(ev);
        assertTrue(createdEvent.getEventId() > 0);

        // Register student
        String email = "booker_" + UUID.randomUUID().toString().substring(0, 8) + "@college.edu";
        Student student = service.registerStudent(new Student("Booker", email, "CSE", 2));

        // Book direct seat
        EventRegistration reg = service.bookDirectSeat(createdEvent.getEventId(), student.getStudentId());
        assertNotNull(reg);
        assertEquals("CONFIRMED", reg.getSeatStatus());

        // Verify available seats decremented from 50 to 49
        Event refreshedEvent = eventDao.findById(createdEvent.getEventId()).orElseThrow();
        assertEquals(49, refreshedEvent.getAvailableSeats());

        // Duplicate booking by same student should be prevented
        assertThrows(IllegalStateException.class, () ->
                service.bookDirectSeat(createdEvent.getEventId(), student.getStudentId())
        );
    }

    @Test
    @DisplayName("Functionality 4 to 8: Complete End-to-End Transfer Flow with Competing Requests & Audit History")
    void testEndToEndSeatTransferAndConcurrency() throws Exception {
        // 1. Create Event with 10 seats
        Event ev = service.createEvent(new Event(
                "CyberSec Summit " + UUID.randomUUID().toString().substring(0, 5),
                LocalDate.now().plusDays(7),
                LocalTime.of(14, 0),
                "Hall C",
                10,
                10,
                LocalDateTime.now().plusDays(6)
        ));

        // 2. Register Student A (Seat Owner)
        Student studentA = service.registerStudent(new Student(
                "Student A",
                "stu_a_" + UUID.randomUUID().toString().substring(0, 6) + "@college.edu",
                "CSE", 3
        ));

        // 3. Register Student B (Requester 1)
        Student studentB = service.registerStudent(new Student(
                "Student B",
                "stu_b_" + UUID.randomUUID().toString().substring(0, 6) + "@college.edu",
                "ECE", 2
        ));

        // 4. Register Student C (Requester 2 - Competing)
        Student studentC = service.registerStudent(new Student(
                "Student C",
                "stu_c_" + UUID.randomUUID().toString().substring(0, 6) + "@college.edu",
                "IT", 4
        ));

        // 5. Student A books a seat
        EventRegistration regA = service.bookDirectSeat(ev.getEventId(), studentA.getStudentId());
        assertEquals("CONFIRMED", regA.getSeatStatus());

        // 6. Student A lists seat for transfer
        EventRegistration listedReg = service.listSeatForTransfer(regA.getRegistrationId(), studentA.getStudentId());
        assertEquals("AVAILABLE_FOR_TRANSFER", listedReg.getSeatStatus());

        // 7. Student A cannot request own seat
        assertThrows(IllegalArgumentException.class, () ->
                service.requestSeatTransfer(listedReg.getRegistrationId(), studentA.getStudentId())
        );

        // 8. Student B submits transfer request
        SeatTransferRequest reqB = service.requestSeatTransfer(listedReg.getRegistrationId(), studentB.getStudentId());
        assertEquals("PENDING", reqB.getTransferStatus());

        // 9. Student C submits transfer request for the SAME seat (competing request)
        SeatTransferRequest reqC = service.requestSeatTransfer(listedReg.getRegistrationId(), studentC.getStudentId());
        assertEquals("PENDING", reqC.getTransferStatus());

        // 10. Student A approves Student B's request
        Map<String, Object> approvalResult = service.approveTransfer(reqB.getTransferId(), studentA.getStudentId());
        assertTrue((Boolean) approvalResult.get("success"));
        assertEquals(studentB.getStudentId(), approvalResult.get("newStudentId"));

        // 11. Verify atomic updates:
        // - Student A's registration should now be TRANSFERRED
        EventRegistration oldReg = registrationDao.findById(regA.getRegistrationId()).orElseThrow();
        assertEquals("TRANSFERRED", oldReg.getSeatStatus());

        // - Student B should have a new CONFIRMED registration
        List<EventRegistration> stuBRegs = registrationDao.findByStudentId(studentB.getStudentId());
        assertTrue(stuBRegs.stream().anyMatch(r -> r.getEventId() == ev.getEventId() && "CONFIRMED".equals(r.getSeatStatus())));

        // - Student B's request status should be APPROVED
        SeatTransferRequest updatedReqB = transferRequestDao.findById(reqB.getTransferId()).orElseThrow();
        assertEquals("APPROVED", updatedReqB.getTransferStatus());

        // - Student C's competing request should be automatically marked REJECTED
        SeatTransferRequest updatedReqC = transferRequestDao.findById(reqC.getTransferId()).orElseThrow();
        assertEquals("REJECTED", updatedReqC.getTransferStatus());

        // - Audit log in SEAT_TRANSFER_HISTORY should contain the transfer
        List<SeatTransferHistory> historyList = service.getTransferHistory();
        assertTrue(historyList.stream().anyMatch(h ->
                h.getTransferId() == reqB.getTransferId() &&
                h.getPreviousStudentId() == studentA.getStudentId() &&
                h.getNewStudentId() == studentB.getStudentId()
        ));
    }
}
