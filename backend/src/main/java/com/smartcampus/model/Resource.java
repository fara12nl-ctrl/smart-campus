package com.smartcampus.model;

import java.time.LocalDateTime;

/**
 * OOP: abstract resource hierarchy. Base implementation of Bookable applies to
 * every room-type resource; subclasses may override the rule (polymorphism).
 */
public abstract class Resource extends CampusEntity implements Bookable {
    protected String roomNo;
    protected int capacity;
    protected String building;

    protected Resource() {}

    protected Resource(String roomNo, int capacity, String building) {
        this.roomNo = roomNo;
        this.capacity = capacity;
        this.building = building;
    }

    @Override
    public boolean isBookable(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null && end.isAfter(start) && !end.isBefore(LocalDateTime.now());
    }

    public abstract String getType();

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    @Override
    public String getKind() { return getType(); }
}
