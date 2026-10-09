package com.apiit.ludot.patterns.strategy;

import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.patterns.nullobject.NullMove;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.dto.MoveCandidate;
import com.apiit.ludot.usecases.dto.MoveDecision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class YellowRacerStrategy implements PlayerStrategy {

    @Override
    public MoveDecision decideMove(Player player, List<MoveCandidate> legalMoves,
                                   GameStateDTO gameState, BoardCoordinateCalculator calculator) {
        if (legalMoves == null || legalMoves.isEmpty()) {
            return NullMove.getInstance();
        }

        for (MoveCandidate m : legalMoves) {
            if (m.moveType() == MoveCandidate.CandidateMoveType.SPAWN_FROM_BASE) {
                return MoveDecision.make(m);
            }
        }

        List<MoveCandidate> neededCaptures = new ArrayList<>();
        for (MoveCandidate m : legalMoves) {
            if ((m.isCapturingOpponent() || m.isCapturingBlockade())
                    && m.piece().getCaptureCount() < 1) {
                neededCaptures.add(m);
            }
        }

        if (!neededCaptures.isEmpty()) {
            neededCaptures.sort(Comparator.comparingInt(MoveCandidate::distanceToHomeAfterMove));
            return MoveDecision.make(neededCaptures.get(0));
        }

        List<MoveCandidate> sortedMoves = new ArrayList<>(legalMoves);
        sortedMoves.sort(Comparator.comparingInt(m -> calculator.calculateDistanceToHome(m.piece())));
        return MoveDecision.make(sortedMoves.get(0));
    }

    @Override
    public String getStrategyName() {
        return "YellowRacerStrategy";
    }
}
