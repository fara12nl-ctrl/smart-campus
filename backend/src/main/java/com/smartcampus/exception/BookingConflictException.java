package com.smartcampus.exception;

/** Thrown when the requested slot overlaps an existing booking. */
public class BookingConflictException extends ApiException {
    public BookingConflictException(String message) {
        super(409, message);
    }
}
