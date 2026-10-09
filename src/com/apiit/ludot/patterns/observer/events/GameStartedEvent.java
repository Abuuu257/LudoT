package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.patterns.observer.GameEvent;

import java.util.List;

public record GameStartedEvent(
        List<Color> participatingColors,
        long timestamp
) implements GameEvent {
    public GameStartedEvent(List<Color> participatingColors) {
        this(participatingColors, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
