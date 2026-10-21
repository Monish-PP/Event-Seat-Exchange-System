package EventSeatExchange.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SeatTransferHistory {
    private int historyId;
    private int transferId;
    private int previousStudentId;
    private int newStudentId;
    private LocalDateTime transferTime;

    // Joined fields for display
    private String previousStudentName;
    private String previousStudentEmail;
    private String previousDepartment;
    private String newStudentName;
    private String newStudentEmail;
    private String newDepartment;
    private int eventId;
    private String eventName;
    private LocalDate eventDate;
    private String venue;

    public SeatTransferHistory() {
    }

    public SeatTransferHistory(int historyId, int transferId, int previousStudentId, int newStudentId, LocalDateTime transferTime) {
        this.historyId = historyId;
        this.transferId = transferId;
        this.previousStudentId = previousStudentId;
        this.newStudentId = newStudentId;
        this.transferTime = transferTime;
    }

    public SeatTransferHistory(int transferId, int previousStudentId, int newStudentId) {
        this.transferId = transferId;
        this.previousStudentId = previousStudentId;
        this.newStudentId = newStudentId;
        this.transferTime = LocalDateTime.now();
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getPreviousStudentId() {
        return previousStudentId;
    }

    public void setPreviousStudentId(int previousStudentId) {
        this.previousStudentId = previousStudentId;
    }

    public int getNewStudentId() {
        return newStudentId;
    }

    public void setNewStudentId(int newStudentId) {
        this.newStudentId = newStudentId;
    }

    public LocalDateTime getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(LocalDateTime transferTime) {
        this.transferTime = transferTime;
    }

    public String getPreviousStudentName() {
        return previousStudentName;
    }

    public void setPreviousStudentName(String previousStudentName) {
        this.previousStudentName = previousStudentName;
    }

    public String getPreviousStudentEmail() {
        return previousStudentEmail;
    }

    public void setPreviousStudentEmail(String previousStudentEmail) {
        this.previousStudentEmail = previousStudentEmail;
    }

    public String getPreviousDepartment() {
        return previousDepartment;
    }

    public void setPreviousDepartment(String previousDepartment) {
        this.previousDepartment = previousDepartment;
    }

    public String getNewStudentName() {
        return newStudentName;
    }

    public void setNewStudentName(String newStudentName) {
        this.newStudentName = newStudentName;
    }

    public String getNewStudentEmail() {
        return newStudentEmail;
    }

    public void setNewStudentEmail(String newStudentEmail) {
        this.newStudentEmail = newStudentEmail;
    }

    public String getNewDepartment() {
        return newDepartment;
    }

    public void setNewDepartment(String newDepartment) {
        this.newDepartment = newDepartment;
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
        return "SeatTransferHistory{" +
                "historyId=" + historyId +
                ", transferId=" + transferId +
                ", previousStudentId=" + previousStudentId +
                ", newStudentId=" + newStudentId +
                ", transferTime=" + transferTime +
                ", eventName='" + eventName + '\'' +
                '}';
    }
}
