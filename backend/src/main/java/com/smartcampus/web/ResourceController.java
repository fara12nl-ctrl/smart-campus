package com.smartcampus.web;

import com.smartcampus.dao.RoomRepository;
import com.smartcampus.dto.ApiResponse;
import com.smartcampus.model.Resource;
import com.smartcampus.service.BookingService;
import com.smartcampus.util.Dates;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Handles both classrooms and laboratories (rooms table with a type discriminator). */
@RestController
@RequestMapping("/api/resources")
public class ResourceController extends AbstractCrudController<Resource> {

    private final BookingService bookingService;

    public ResourceController(RoomRepository repo, BookingService bookingService) {
        super(repo);
        this.bookingService = bookingService;
    }

    @Override
    protected String searchText(Resource r) {
        return r.getRoomNo() + " " + r.getType() + " " + r.getBuilding() + " " + r.getCapacity();
    }

    @GetMapping("/classrooms")
    public ApiResponse<List<Resource>> classrooms() {
        return ApiResponse.ok(repo.findAll().stream()
                .filter(r -> "CLASSROOM".equals(r.getType()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/labs")
    public ApiResponse<List<Resource>> labs() {
        return ApiResponse.ok(repo.findAll().stream()
                .filter(r -> "LAB".equals(r.getType()))
                .collect(Collectors.toList()));
    }

    /** Availability check for a time window + existing bookings. */
    @GetMapping("/{id}/availability")
    public ApiResponse<Map<String, Object>> availability(@PathVariable long id,
                                                         @RequestParam String start,
                                                         @RequestParam String end) {
        Resource room = repo.findById(id)
                .orElseThrow(() -> new com.smartcampus.exception.NotFoundException("Room", id));
        LocalDateTime s = Dates.parse(start);
        LocalDateTime e = Dates.parse(end);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("resource", room);
        data.put("bookable", room.isBookable(s, e));
        data.put("bookings", bookingService.forResource("ROOM", id));
        return ApiResponse.ok(data);
    }
}
