package com.smartcampus.model;

public class Booking extends CampusEntity {
    private String resourceType; // ROOM | EQUIPMENT
    private long resourceId;
    private String bookedBy;
    private String purpose;
    private String startTime;    // ISO local date-time
    private String endTime;      // ISO local date-time
    private String status;       // CONFIRMED | CANCELLED

    public Booking() {}

    public Booking(long id, String resourceType, long resourceId, String bookedBy,
                   String purpose, String startTime, String endTime, String status) {
        this.id = id;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.bookedBy = bookedBy;
        this.purpose = purpose;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    @Override
    public String getKind() { return "BOOKING"; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public long getResourceId() { return resourceId; }
    public void setResourceId(long resourceId) { this.resourceId = resourceId; }
    public String getBookedBy() { return bookedBy; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
