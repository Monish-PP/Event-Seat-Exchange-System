package EventSeatExchange.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Event {
    private int eventId;
    private String eventName;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String venue;
    private int totalSeats;
    private int availableSeats;
    private LocalDateTime registrationDeadline;

    public Event() {
    }

    public Event(int eventId, String eventName, LocalDate eventDate, LocalTime eventTime, String venue,
                 int totalSeats, int availableSeats, LocalDateTime registrationDeadline) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.registrationDeadline = registrationDeadline;
    }

    public Event(String eventName, LocalDate eventDate, LocalTime eventTime, String venue,
                 int totalSeats, int availableSeats, LocalDateTime registrationDeadline) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.registrationDeadline = registrationDeadline;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
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

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public boolean isRegistrationOpen() {
        return availableSeats > 0 &&
                (registrationDeadline == null || LocalDateTime.now().isBefore(registrationDeadline));
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", eventName='" + eventName + '\'' +
                ", eventDate=" + eventDate +
                ", eventTime=" + eventTime +
                ", venue='" + venue + '\'' +
                ", totalSeats=" + totalSeats +
                ", availableSeats=" + availableSeats +
                ", registrationDeadline=" + registrationDeadline +
                '}';
    }
}
