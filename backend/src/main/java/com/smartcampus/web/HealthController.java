package com.smartcampus.web;

import com.smartcampus.dto.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbc;

    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            data.put("database", one != null && one == 1 ? "CONNECTED" : "UNKNOWN");
        } catch (Exception e) {
            data.put("database", "DOWN: " + e.getMessage());
        }
        return ApiResponse.ok(data);
    }
}
