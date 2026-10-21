package EventSeatExchange.cli;

import EventSeatExchange.model.*;
import EventSeatExchange.service.SeatExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Component
public class SeatExchangeCliRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SeatExchangeCliRunner.class);
    private final SeatExchangeService service;

    public SeatExchangeCliRunner(SeatExchangeService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        boolean cliMode = Arrays.asList(args).contains("--cli");
        if (!cliMode) {
            log.info("==========================================================================");
            log.info("EVENT SEAT EXCHANGE APP STARTED!");
            log.info("Web Application UI available at: http://localhost:8080");
            log.info("To run in terminal CLI mode, launch with: java -jar app.jar --cli");
            log.info("==========================================================================");
            return;
        }

        startInteractiveCli();
    }

    public void startInteractiveCli() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n========================================================");
        System.out.println("  WELCOME TO EVENT SEAT EXCHANGE CONSOLE CLI");
        System.out.println("========================================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Register Student");
            System.out.println("2. Add College Event");
            System.out.println("3. View Events and Seat Availability");
            System.out.println("4. Book Seat for Event (Direct)");
            System.out.println("5. List Confirmed Seat for Transfer");
            System.out.println("6. Browse Available Seats & Request Transfer");
            System.out.println("7. Manage Transfer Requests (Approve / Reject)");
            System.out.println("8. View Seat Transfer History");
            System.out.println("9. View System Statistics");
            System.out.println("0. Exit CLI");
            System.out.print("Select an option [0-9]: ");

            String input = scanner.nextLine().trim();
            try {
                switch (input) {
                    case "1":
                        handleRegisterStudent(scanner);
                        break;
                    case "2":
                        handleAddEvent(scanner);
                        break;
                    case "3":
                        handleViewEvents();
                        break;
                    case "4":
                        handleDirectBooking(scanner);
                        break;
                    case "5":
                        handleListSeatForTransfer(scanner);
                        break;
                    case "6":
                        handleRequestTransfer(scanner);
                        break;
                    case "7":
                        handleManageRequests(scanner);
                        break;
                    case "8":
                        handleViewHistory();
                        break;
                    case "9":
                        handleViewStats();
                        break;
                    case "0":
                        running = false;
                        System.out.println("Exiting Console CLI. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please enter a number between 0 and 9.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void handleRegisterStudent(Scanner scanner) throws Exception {
        System.out.print("Enter Student Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Department (e.g. CSE, IT, ECE): ");
        String dept = scanner.nextLine().trim();
        System.out.print("Enter Year of Study (1-4): ");
        int year = Integer.parseInt(scanner.nextLine().trim());

        Student student = new Student(name, email, dept, year);
        service.registerStudent(student);
        System.out.printf("Student registered successfully! Generated Student ID: %d\n", student.getStudentId());
    }

    private void handleAddEvent(Scanner scanner) throws Exception {
        System.out.print("Enter Event Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Event Date (YYYY-MM-DD): ");
        LocalDate date = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Enter Event Time (HH:MM:SS): ");
        LocalTime time = LocalTime.parse(scanner.nextLine().trim());
        System.out.print("Enter Venue: ");
        String venue = scanner.nextLine().trim();
        System.out.print("Enter Total Seat Capacity: ");
        int seats = Integer.parseInt(scanner.nextLine().trim());

        LocalDateTime deadline = LocalDateTime.of(date, time).minusHours(2);
        Event event = new Event(name, date, time, venue, seats, seats, deadline);
        service.createEvent(event);
        System.out.printf("Event created successfully! Generated Event ID: %d\n", event.getEventId());
    }

    private void handleViewEvents() throws Exception {
        List<Event> events = service.getAllEvents();
        System.out.println("\n--- UPCOMING & REGISTERED EVENTS ---");
        System.out.printf("%-5s | %-30s | %-12s | %-10s | %-20s | %-15s\n", "ID", "Event Name", "Date", "Time", "Venue", "Seats Available");
        System.out.println("------------------------------------------------------------------------------------------------------");
        for (Event e : events) {
            System.out.printf("%-5d | %-30s | %-12s | %-10s | %-20s | %d / %d\n",
                    e.getEventId(), e.getEventName(), e.getEventDate(), e.getEventTime(), e.getVenue(), e.getAvailableSeats(), e.getTotalSeats());
        }
    }

    private void handleDirectBooking(Scanner scanner) throws Exception {
        System.out.print("Enter Event ID: ");
        int eventId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter Student ID: ");
        int studentId = Integer.parseInt(scanner.nextLine().trim());

        EventRegistration reg = service.bookDirectSeat(eventId, studentId);
        System.out.printf("Seat booked successfully! Registration ID: %d, Status: %s\n", reg.getRegistrationId(), reg.getSeatStatus());
    }

    private void handleListSeatForTransfer(Scanner scanner) throws Exception {
        System.out.print("Enter Your Student ID: ");
        int studentId = Integer.parseInt(scanner.nextLine().trim());

        List<EventRegistration> regs = service.getRegistrationsByStudent(studentId);
        if (regs.isEmpty()) {
            System.out.println("You have no registrations.");
            return;
        }

        System.out.println("\nYour Current Registrations:");
        for (EventRegistration r : regs) {
            System.out.printf("Registration ID: %d | Event: %s | Status: %s\n", r.getRegistrationId(), r.getEventName(), r.getSeatStatus());
        }

        System.out.print("Enter Registration ID to list for transfer: ");
        int regId = Integer.parseInt(scanner.nextLine().trim());

        EventRegistration updated = service.listSeatForTransfer(regId, studentId);
        System.out.printf("Success! Registration %d is now AVAILABLE_FOR_TRANSFER.\n", updated.getRegistrationId());
    }

    private void handleRequestTransfer(Scanner scanner) throws Exception {
        List<EventRegistration> seats = service.getSeatsAvailableForTransfer();
        if (seats.isEmpty()) {
            System.out.println("No seats currently listed for transfer.");
            return;
        }

        System.out.println("\n--- SEATS AVAILABLE FOR TRANSFER ---");
        for (EventRegistration s : seats) {
            System.out.printf("Reg ID: %d | Event: %s | Listed By: %s (%s) | Date: %s\n",
                    s.getRegistrationId(), s.getEventName(), s.getStudentName(), s.getDepartment(), s.getEventDate());
        }

        System.out.print("Enter Registration ID you wish to request: ");
        int regId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter Your Student ID: ");
        int studentId = Integer.parseInt(scanner.nextLine().trim());

        SeatTransferRequest req = service.requestSeatTransfer(regId, studentId);
        System.out.printf("Transfer request submitted! Transfer ID: %d, Status: %s\n", req.getTransferId(), req.getTransferStatus());
    }

    private void handleManageRequests(Scanner scanner) throws Exception {
        System.out.print("Enter Your Student ID (Seat Owner): ");
        int ownerId = Integer.parseInt(scanner.nextLine().trim());

        List<SeatTransferRequest> requests = service.getIncomingTransferRequests(ownerId);
        if (requests.isEmpty()) {
            System.out.println("No incoming transfer requests found for your seats.");
            return;
        }

        System.out.println("\n--- INCOMING TRANSFER REQUESTS ---");
        for (SeatTransferRequest r : requests) {
            System.out.printf("Transfer ID: %d | Event: %s | Requester: %s (%s) | Status: %s\n",
                    r.getTransferId(), r.getEventName(), r.getRequesterName(), r.getRequesterEmail(), r.getTransferStatus());
        }

        System.out.print("Enter Transfer ID to process: ");
        int transferId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Action (1 = Approve, 2 = Reject): ");
        String action = scanner.nextLine().trim();

        if ("1".equals(action)) {
            Map<String, Object> res = service.approveTransfer(transferId, ownerId);
            System.out.printf("APPROVED! Seat transferred to student %s. Auto-rejected %s competing requests.\n",
                    res.get("newStudentId"), res.get("rejectedOtherPendingCount"));
        } else if ("2".equals(action)) {
            service.rejectTransfer(transferId, ownerId);
            System.out.println("Transfer request REJECTED.");
        } else {
            System.out.println("Invalid action.");
        }
    }

    private void handleViewHistory() throws Exception {
        List<SeatTransferHistory> history = service.getTransferHistory();
        System.out.println("\n--- COMPLETED SEAT TRANSFER AUDIT LOG ---");
        System.out.printf("%-5s | %-25s | %-20s | %-20s | %-20s\n", "ID", "Event", "From Student", "To Student", "Transfer Time");
        System.out.println("---------------------------------------------------------------------------------------------------");
        for (SeatTransferHistory h : history) {
            System.out.printf("%-5d | %-25s | %-20s | %-20s | %s\n",
                    h.getHistoryId(), h.getEventName(), h.getPreviousStudentName(), h.getNewStudentName(), h.getTransferTime());
        }
    }

    private void handleViewStats() throws Exception {
        Map<String, Object> stats = service.getDashboardStats();
        System.out.println("\n--- SYSTEM DASHBOARD METRICS ---");
        System.out.println("Total Events: " + stats.get("totalEvents"));
        System.out.println("Total Available Direct Seats: " + stats.get("totalDirectAvailableSeats"));
        System.out.println("Seats Listed For Exchange: " + stats.get("seatsListedForExchange"));
        System.out.println("Completed Transfers: " + stats.get("completedTransfers"));
        System.out.println("Total Registered Students: " + stats.get("totalStudents"));
    }
}
