package com.smartcampus.web;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.RaceResult;
import com.smartcampus.service.RaceDemoService;
import com.smartcampus.util.Dates;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Multithreading demo: N threads race for the same booking slot; only one wins. */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final RaceDemoService raceDemo;

    public DemoController(RaceDemoService raceDemo) {
        this.raceDemo = raceDemo;
    }

    @GetMapping("/race")
    public ApiResponse<RaceResult> race(
            @RequestParam(defaultValue = "ROOM") String type,
            @RequestParam(defaultValue = "1") long id,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "5") int threads) throws InterruptedException {
        String s = start != null ? start : Dates.iso(LocalDate.now().plusDays(1).atTime(10, 0));
        String e = end != null ? end : Dates.iso(LocalDate.now().plusDays(1).atTime(11, 0));
        RaceResult result = raceDemo.run(type.toUpperCase(), id, s, e, threads);
        return ApiResponse.ok("Race completed: " + result.getSuccesses() + " success / "
                + result.getConflicts() + " conflicts", result);
    }
}
