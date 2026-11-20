package com.smartcampus.model;

/** OOP: abstract base class shared by every entity (inheritance root). */
public abstract class CampusEntity implements Identifiable {
    protected long id;

    protected CampusEntity() {}

    protected CampusEntity(long id) {
        this.id = id;
    }

    @Override
    public long getId() { return id; }

    @Override
    public void setId(long id) { this.id = id; }

    public abstract String getKind();
}
