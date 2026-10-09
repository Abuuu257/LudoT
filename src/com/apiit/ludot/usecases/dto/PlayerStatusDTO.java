package com.apiit.ludot.usecases.dto;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Player;

import java.util.List;

public record PlayerStatusDTO(
        Color color,
        int piecesOnBoardCount,
        int piecesInBaseCount,
        int piecesInHomeCount,
        List<PieceDTO> pieces
) {
    public static PlayerStatusDTO fromEntity(Player player) {
        List<PieceDTO> dtos = player.getPieces().stream()
                .map(PieceDTO::fromEntity)
                .toList();

        return new PlayerStatusDTO(
                player.getColor(),
                player.getPiecesOnBoard().size(),
                player.getPiecesInBase().size(),
                player.getPiecesInHome().size(),
                dtos
        );
    }
}
