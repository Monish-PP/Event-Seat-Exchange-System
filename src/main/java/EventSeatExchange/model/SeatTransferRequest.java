package EventSeatExchange.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SeatTransferRequest {
    private int transferId;
    private int registrationId;
    private int requestedBy;
    private LocalDateTime requestTime;
    private String transferStatus; // PENDING, APPROVED, REJECTED, CANCELLED

    // Joined fields for display
    private String requesterName;
    private String requesterEmail;
    private String requesterDepartment;
    private int currentOwnerId;
    private String currentOwnerName;
    private String currentOwnerEmail;
    private int eventId;
    private String eventName;
    private LocalDate eventDate;
    private String venue;

    public SeatTransferRequest() {
    }

    public SeatTransferRequest(int transferId, int registrationId, int requestedBy, LocalDateTime requestTime, String transferStatus) {
        this.transferId = transferId;
        this.registrationId = registrationId;
        this.requestedBy = requestedBy;
        this.requestTime = requestTime;
        this.transferStatus = transferStatus;
    }

    public SeatTransferRequest(int registrationId, int requestedBy, String transferStatus) {
        this.registrationId = registrationId;
        this.requestedBy = requestedBy;
        this.transferStatus = transferStatus;
        this.requestTime = LocalDateTime.now();
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(int requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(String transferStatus) {
        this.transferStatus = transferStatus;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getRequesterDepartment() {
        return requesterDepartment;
    }

    public void setRequesterDepartment(String requesterDepartment) {
        this.requesterDepartment = requesterDepartment;
    }

    public int getCurrentOwnerId() {
        return currentOwnerId;
    }

    public void setCurrentOwnerId(int currentOwnerId) {
        this.currentOwnerId = currentOwnerId;
    }

    public String getCurrentOwnerName() {
        return currentOwnerName;
    }

    public void setCurrentOwnerName(String currentOwnerName) {
        this.currentOwnerName = currentOwnerName;
    }

    public String getCurrentOwnerEmail() {
        return currentOwnerEmail;
    }

    public void setCurrentOwnerEmail(String currentOwnerEmail) {
        this.currentOwnerEmail = currentOwnerEmail;
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

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    @Override
    public String toString() {
        return "SeatTransferRequest{" +
                "transferId=" + transferId +
                ", registrationId=" + registrationId +
                ", requestedBy=" + requestedBy +
                ", requestTime=" + requestTime +
                ", transferStatus='" + transferStatus + '\'' +
                ", requesterName='" + requesterName + '\'' +
                ", eventName='" + eventName + '\'' +
                '}';
    }
}
