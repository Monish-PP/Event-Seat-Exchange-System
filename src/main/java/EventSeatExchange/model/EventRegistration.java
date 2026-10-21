package EventSeatExchange.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventRegistration {
    private int registrationId;
    private int eventId;
    private int studentId;
    private LocalDateTime registrationTime;
    private String seatStatus; // CONFIRMED, AVAILABLE_FOR_TRANSFER, TRANSFERRED

    // Joined fields for display
    private String eventName;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String venue;
    private String studentName;
    private String studentEmail;
    private String department;

    public EventRegistration() {
    }

    public EventRegistration(int registrationId, int eventId, int studentId, LocalDateTime registrationTime, String seatStatus) {
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.studentId = studentId;
        this.registrationTime = registrationTime;
        this.seatStatus = seatStatus;
    }

    public EventRegistration(int eventId, int studentId, String seatStatus) {
        this.eventId = eventId;
        this.studentId = studentId;
        this.seatStatus = seatStatus;
        this.registrationTime = LocalDateTime.now();
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getSeatStatus() {
        return seatStatus;
    }

    public void setSeatStatus(String seatStatus) {
        this.seatStatus = seatStatus;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "EventRegistration{" +
                "registrationId=" + registrationId +
                ", eventId=" + eventId +
                ", studentId=" + studentId +
                ", registrationTime=" + registrationTime +
                ", seatStatus='" + seatStatus + '\'' +
                ", eventName='" + eventName + '\'' +
                '}';
    }
}
