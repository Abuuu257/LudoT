package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.patterns.observer.GameEvent;

public record PieceSpawnedEvent(
        Color playerColor,
        String pieceId,
        Direction direction,
        String coinTossOutcome,
        int piecesOnBoard,
        int piecesInBase,
        long timestamp
) implements GameEvent {
    public PieceSpawnedEvent(Color playerColor, String pieceId, Direction direction, String coinTossOutcome, int piecesOnBoard, int piecesInBase) {
        this(playerColor, pieceId, direction, coinTossOutcome, piecesOnBoard, piecesInBase, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
