package com.apiit.ludot.usecases.dto;

import com.apiit.ludot.domain.model.Color;

import java.util.List;

public record GameStateDTO(
        int roundNumber,
        Color currentPlayerColor,
        int lastDiceRoll,
        int mysteryCellLocation,
        int mysteryRoundsRemaining,
        List<PlayerStatusDTO> players,
        boolean isGameOver,
        Color winnerColor
) {
    public PlayerStatusDTO getPlayerStatus(Color color) {
        for (PlayerStatusDTO p : players) {
            if (p.color() == color) {
                return p;
            }
        }
        return null;
    }
}
