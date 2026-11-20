package com.smartcampus.model;

import java.time.LocalDateTime;

/** OOP: interface with a domain rule implemented polymorphically by each bookable resource. */
public interface Bookable {
    boolean isBookable(LocalDateTime start, LocalDateTime end);
}
