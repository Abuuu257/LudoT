package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.patterns.observer.GameEvent;

public record PieceMovedEvent(
        Color playerColor,
        String pieceId,
        String fromLocation,
        String toLocation,
        int units,
        Direction direction,
        long timestamp
) implements GameEvent {
    public PieceMovedEvent(Color playerColor, String pieceId, String fromLocation, String toLocation, int units, Direction direction) {
        this(playerColor, pieceId, fromLocation, toLocation, units, direction, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
