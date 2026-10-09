package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.patterns.observer.GameEvent;

public record MysterySpawnedEvent(
        String locationL1,
        int durationRounds,
        long timestamp
) implements GameEvent {
    public MysterySpawnedEvent(String locationL1, int durationRounds) {
        this(locationL1, durationRounds, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
