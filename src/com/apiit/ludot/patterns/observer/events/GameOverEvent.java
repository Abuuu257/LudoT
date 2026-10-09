package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.patterns.observer.GameEvent;

public record GameOverEvent(
        Color winnerColor,
        int totalRounds,
        int finishPosition,
        int movesTaken,
        boolean isSummary,
        long timestamp
) implements GameEvent {
    public GameOverEvent(Color winnerColor, int totalRounds, int finishPosition, int movesTaken, boolean isSummary) {
        this(winnerColor, totalRounds, finishPosition, movesTaken, isSummary, System.currentTimeMillis());
    }

    public GameOverEvent(Color winnerColor, int totalRounds, int finishPosition, int movesTaken) {
        this(winnerColor, totalRounds, finishPosition, movesTaken, false, System.currentTimeMillis());
    }

    public GameOverEvent(Color winnerColor, int totalRounds, int finishPosition) {
        this(winnerColor, totalRounds, finishPosition, 0, false, System.currentTimeMillis());
    }

    public GameOverEvent(Color winnerColor, int totalRounds) {
        this(winnerColor, totalRounds, 1, 0, false, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
