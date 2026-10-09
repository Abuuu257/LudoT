package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.patterns.observer.GameEvent;

import java.util.List;

public record PieceCapturedEvent(
        Color capturingColor,
        String capturingPieceName,
        String locationL1,
        Color capturedColor,
        List<String> capturedPieceNames,
        int capturerPiecesOnBoard,
        int capturerPiecesInBase,
        boolean isBlockadeCapture,
        long timestamp
) implements GameEvent {
    public PieceCapturedEvent(Color capturingColor, String capturingPieceName, String locationL1,
                              Color capturedColor, List<String> capturedPieceNames,
                              int capturerPiecesOnBoard, int capturerPiecesInBase,
                              boolean isBlockadeCapture) {
        this(capturingColor, capturingPieceName, locationL1, capturedColor, capturedPieceNames,
                capturerPiecesOnBoard, capturerPiecesInBase, isBlockadeCapture, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
