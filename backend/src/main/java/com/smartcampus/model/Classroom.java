package com.smartcampus.model;

public class Classroom extends Resource {

    public Classroom() {}

    public Classroom(long id, String roomNo, int capacity, String building) {
        super(roomNo, capacity, building);
        this.id = id;
    }

    @Override
    public String getType() { return "CLASSROOM"; }
}
