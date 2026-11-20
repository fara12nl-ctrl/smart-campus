package com.smartcampus.web;

import com.smartcampus.dao.BookingRepository;
import com.smartcampus.dto.ApiResponse;
import com.smartcampus.model.Booking;
import com.smartcampus.model.UserAccount;
import com.smartcampus.security.AuthInterceptor;
import com.smartcampus.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository repo;
    private final BookingService service;

    public BookingController(BookingRepository repo, BookingService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Booking>> list(@RequestParam(required = false) String status) {
        List<Booking> all = repo.findAll();
        if (status != null && !status.isBlank()) {
            all = all.stream()
                    .filter(b -> status.equalsIgnoreCase(b.getStatus()))
                    .toList();
        }
        return ApiResponse.ok(all);
    }

    @GetMapping("/{id}")
    public ApiResponse<Booking> get(@PathVariable long id) {
        return ApiResponse.ok(repo.getOrThrow(id));
    }

    @PostMapping
    public ApiResponse<Booking> create(@RequestBody Booking booking, HttpServletRequest request) {
        UserAccount user = (UserAccount) request.getAttribute(AuthInterceptor.ATTR_USER);
        if (booking.getBookedBy() == null || booking.getBookedBy().isBlank()) {
            booking.setBookedBy(user != null ? user.getDisplayName() : "Unknown");
        }
        long id = service.book(booking);
        return ApiResponse.ok("Booking confirmed", repo.getOrThrow(id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Booking> cancel(@PathVariable long id) {
        Booking b = repo.getOrThrow(id);
        b.setStatus("CANCELLED");
        repo.update(id, b);
        return ApiResponse.ok("Booking cancelled", repo.getOrThrow(id));
    }

    @GetMapping("/resource/{type}/{resourceId}")
    public ApiResponse<List<Booking>> forResource(@PathVariable String type, @PathVariable long resourceId) {
        return ApiResponse.ok(service.forResource(type, resourceId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        repo.delete(id);
        return ApiResponse.ok("Deleted", null);
    }
}
