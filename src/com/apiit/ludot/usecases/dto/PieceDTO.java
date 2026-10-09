package com.apiit.ludot.usecases.dto;

import com.apiit.ludot.domain.model.AuraEffect;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;

public record PieceDTO(
        String id,
        int pieceNumber,
        Color color,
        String cellId,
        int trackIndex,
        int homeStraightIndex,
        Direction direction,
        int captures,
        AuraEffect aura,
        boolean isAtBase,
        boolean isInHome,
        boolean isInHomeStraight,
        boolean isRestricted
) {
    public static PieceDTO fromEntity(Piece piece) {
        return new PieceDTO(
                piece.getId(),
                piece.getPieceNumber(),
                piece.getColor(),
                piece.getCurrentCellId(),
                piece.getTrackIndex(),
                piece.getHomeStraightIndex(),
                piece.getCurrentDirection(),
                piece.getCaptureCount(),
                piece.getAuraEffect(),
                piece.getState().isAtBase(),
                piece.getState().isInHome(),
                piece.getState().isInHomeStraight(),
                piece.getState().isRestricted()
        );
    }
}
