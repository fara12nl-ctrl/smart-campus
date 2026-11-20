package com.smartcampus.dto;

import java.util.List;

/** Result of the multithreading demo: N threads race for the same booking slot. */
public class RaceResult {
    private int attempts;
    private int successes;
    private int conflicts;
    private long durationMs;
    private List<String> winners;

    public RaceResult(int attempts, int successes, int conflicts, long durationMs, List<String> winners) {
        this.attempts = attempts;
        this.successes = successes;
        this.conflicts = conflicts;
        this.durationMs = durationMs;
        this.winners = winners;
    }

    public int getAttempts() { return attempts; }
    public int getSuccesses() { return successes; }
    public int getConflicts() { return conflicts; }
    public long getDurationMs() { return durationMs; }
    public List<String> getWinners() { return winners; }
}
