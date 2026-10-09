package com.apiit.ludot.patterns.strategy;

import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.patterns.nullobject.NullMove;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.dto.MoveCandidate;
import com.apiit.ludot.usecases.dto.MoveDecision;

import java.util.List;

public class NullPlayerStrategy implements PlayerStrategy {

    @Override
    public MoveDecision decideMove(Player player, List<MoveCandidate> legalMoves,
                            GameStateDTO gameState, BoardCoordinateCalculator calculator) {
        if (legalMoves != null && !legalMoves.isEmpty()) {
            return MoveDecision.make(legalMoves.get(0));
        }
        return NullMove.getInstance();
    }

    @Override
    public String getStrategyName() {
        return "NullStrategy";
    }
}
