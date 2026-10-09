package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.patterns.observer.GameEvent;

import java.util.List;
import java.util.Map;

public record FirstPlayerChosenEvent(
        Map<Color, Integer> initialRolls,
        Color chosenStartingPlayer,
        List<Color> roundOrder,
        long timestamp
) implements GameEvent {
    public FirstPlayerChosenEvent(Map<Color, Integer> initialRolls, Color chosenStartingPlayer, List<Color> roundOrder) {
        this(initialRolls, chosenStartingPlayer, roundOrder, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
