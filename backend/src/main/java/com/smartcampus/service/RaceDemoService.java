package com.smartcampus.service;

import com.smartcampus.model.Booking;
import com.smartcampus.dto.RaceResult;
import com.smartcampus.exception.ApiException;
import com.smartcampus.exception.BookingConflictException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Fires N threads at the same booking slot to prove synchronization prevents double-booking. */
@Service
public class RaceDemoService {

    private final BookingService bookingService;

    public RaceDemoService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public RaceResult run(String resourceType, long resourceId, String start, String end, int threads)
            throws InterruptedException {
        if (threads < 2 || threads > 20) throw new ApiException(400, "threads must be between 2 and 20");
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        List<String> winners = new CopyOnWriteArrayList<>();
        long t0 = System.currentTimeMillis();

        for (int i = 1; i <= threads; i++) {
            final int n = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();
                    Booking b = new Booking();
                    b.setResourceType(resourceType);
                    b.setResourceId(resourceId);
                    b.setBookedBy("DemoThread-" + n);
                    b.setPurpose("Race demo booking");
                    b.setStartTime(start);
                    b.setEndTime(end);
                    bookingService.book(b);
                    successes.incrementAndGet();
                    winners.add("DemoThread-" + n);
                } catch (BookingConflictException e) {
                    conflicts.incrementAndGet();
                } catch (ApiException e) {
                    if (e.getStatus() == 409) conflicts.incrementAndGet();
                    else throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        ready.await(10, TimeUnit.SECONDS);
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        return new RaceResult(threads, successes.get(), conflicts.get(),
                System.currentTimeMillis() - t0, winners);
    }
}
