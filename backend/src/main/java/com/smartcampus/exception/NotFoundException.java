package com.smartcampus.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(String what, long id) {
        super(404, what + " with id " + id + " was not found");
    }

    public NotFoundException(String message) {
        super(404, message);
    }
}
