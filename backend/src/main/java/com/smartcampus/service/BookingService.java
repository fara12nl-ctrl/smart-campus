package com.smartcampus.service;

import com.smartcampus.dao.BookingRepository;
import com.smartcampus.dao.EquipmentRepository;
import com.smartcampus.dao.RoomRepository;
import com.smartcampus.exception.BookingConflictException;
import com.smartcampus.exception.ValidationException;
import com.smartcampus.model.Booking;
import com.smartcampus.model.Bookable;
import com.smartcampus.model.Equipment;
import com.smartcampus.model.Resource;
import com.smartcampus.util.Dates;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BookingService - the multithreading/synchronization centerpiece.
 * book() is synchronized so simultaneous requests can never double-book a slot;
 * the DB-level overlap query is a second safety net.
 */
@Service
public class BookingService {

    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final EquipmentRepository equipment;

    public BookingService(BookingRepository bookings, RoomRepository rooms, EquipmentRepository equipment) {
        this.bookings = bookings;
        this.rooms = rooms;
        this.equipment = equipment;
    }

    public synchronized long book(Booking b) {
        if (b.getResourceType() == null) throw new ValidationException("resourceType is required (ROOM or EQUIPMENT)");
        String type = b.getResourceType().toUpperCase();
        if (!type.equals("ROOM") && !type.equals("EQUIPMENT")) {
            throw new ValidationException("resourceType must be ROOM or EQUIPMENT");
        }
        b.setResourceType(type);
        LocalDateTime start = Dates.parse(b.getStartTime());
        LocalDateTime end = Dates.parse(b.getEndTime());
        if (!end.isAfter(start)) throw new ValidationException("endTime must be after startTime");
        if (end.isBefore(LocalDateTime.now())) throw new ValidationException("Cannot book a slot in the past");

        Bookable resource = resolve(type, b.getResourceId());
        if (!resource.isBookable(start, end)) {
            throw new ValidationException("Resource is not bookable for the selected time window");
        }
        if (bookings.hasOverlap(type, b.getResourceId(), start, end)) {
            throw new BookingConflictException("Resource is already booked for this time window");
        }
        if (b.getBookedBy() == null || b.getBookedBy().isBlank()) {
            throw new ValidationException("bookedBy is required");
        }
        if (b.getPurpose() == null || b.getPurpose().isBlank()) {
            b.setPurpose("General booking");
        }
        if (b.getStatus() == null) b.setStatus("CONFIRMED");
        return bookings.create(b);
    }

    public List<Booking> forResource(String type, long id) {
        return bookings.findByResource(type.toUpperCase(), id);
    }

    private Bookable resolve(String type, long id) {
        if (type.equals("ROOM")) {
            Resource r = rooms.findById(id)
                    .orElseThrow(() -> new ValidationException("Room " + id + " does not exist"));
            return r;
        }
        Equipment e = equipment.findById(id)
                .orElseThrow(() -> new ValidationException("Equipment " + id + " does not exist"));
        return e;
    }
}
