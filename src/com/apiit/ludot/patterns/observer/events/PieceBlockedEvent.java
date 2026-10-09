package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.patterns.observer.GameEvent;

public record PieceBlockedEvent(
        Color playerColor,
        String pieceName,
        String fromLocation,
        String intendedLocation,
        Color blockingColor,
        String blockingPieceName,
        boolean movedToAdjacentBeforeBlock,
        String adjacentLocationL3,
        long timestamp
) implements GameEvent {
    public PieceBlockedEvent(Color playerColor, String pieceName, String fromLocation, String intendedLocation,
                             Color blockingColor, String blockingPieceName, boolean movedToAdjacentBeforeBlock,
                             String adjacentLocationL3) {
        this(playerColor, pieceName, fromLocation, intendedLocation, blockingColor, blockingPieceName,
                movedToAdjacentBeforeBlock, adjacentLocationL3, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
