package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.patterns.observer.GameEvent;

public record DiceRolledEvent(
        Color playerColor,
        int rollValue,
        long timestamp
) implements GameEvent {
    public DiceRolledEvent(Color playerColor, int rollValue) {
        this(playerColor, rollValue, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
