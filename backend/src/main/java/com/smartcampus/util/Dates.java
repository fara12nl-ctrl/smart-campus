package com.smartcampus.util;

import com.smartcampus.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Dates {
    private static final DateTimeFormatter DB = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Dates() {}

    /** Accepts "2026-11-21 10:00", "2026-11-21 10:00:00" and ISO "2026-11-21T10:00:00". */
    public static LocalDateTime parse(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Date-time value is required");
        }
        String v = value.trim().replace(' ', 'T');
        try {
            return LocalDateTime.parse(v);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(value.trim(), DB);
            } catch (Exception e2) {
                throw new ValidationException("Invalid date-time: '" + value + "' (expected yyyy-MM-dd HH:mm)");
            }
        }
    }

    public static String iso(LocalDateTime value) {
        return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static String db(LocalDateTime value) {
        return value == null ? null : value.format(DB);
    }
}
