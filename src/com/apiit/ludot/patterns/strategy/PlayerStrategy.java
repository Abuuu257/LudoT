package com.apiit.ludot.patterns.strategy;

import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.dto.MoveCandidate;
import com.apiit.ludot.usecases.dto.MoveDecision;

import java.util.List;

public interface PlayerStrategy {
    MoveDecision decideMove(Player player, List<MoveCandidate> legalMoves,
                            GameStateDTO gameState, BoardCoordinateCalculator calculator);
    String getStrategyName();
}
