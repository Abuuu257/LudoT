package com.apiit.ludot.usecases.dto;

import com.apiit.ludot.domain.model.Piece;

import java.util.List;

public record MoveResultDTO(
        boolean success,
        boolean bonusRollEarned,
        boolean pieceCaptured,
        List<Piece> capturedPieces,
        boolean isBlocked,
        String fromCell,
        String toCell,
        String pieceId,
        String description
) {
    public static MoveResultDTO failed(String reason) {
        return new MoveResultDTO(false, false, false, List.of(), false, "", "", "", reason);
    }
}
