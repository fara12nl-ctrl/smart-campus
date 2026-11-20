package com.smartcampus.model;

/** OOP: interface implemented by every persisted entity. */
public interface Identifiable {
    long getId();
    void setId(long id);
}
