package com.smartcampus.model;

import java.time.Duration;
import java.time.LocalDateTime;

/** OOP: overrides the base booking rule (labs allow slots of at most 4 hours). */
public class Laboratory extends Resource {

    public Laboratory() {}

    public Laboratory(long id, String roomNo, int capacity, String building) {
        super(roomNo, capacity, building);
        this.id = id;
    }

    @Override
    public String getType() { return "LAB"; }

    @Override
    public boolean isBookable(LocalDateTime start, LocalDateTime end) {
        return super.isBookable(start, end)
                && Duration.between(start, end).toHours() <= 4;
    }
}
