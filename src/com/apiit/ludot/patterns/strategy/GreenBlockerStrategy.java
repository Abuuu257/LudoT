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

public class GreenBlockerStrategy implements PlayerStrategy {

    @Override
    public MoveDecision decideMove(Player player, List<MoveCandidate> legalMoves,
                                   GameStateDTO gameState, BoardCoordinateCalculator calculator) {
        if (legalMoves == null || legalMoves.isEmpty()) {
            return NullMove.getInstance();
        }

        MoveCandidate blockCreatingMove = null;
        MoveCandidate spawnMove = null;

        for (MoveCandidate m : legalMoves) {
            if (m.isCreatingBlockade()) {
                blockCreatingMove = m;
            }
            if (m.moveType() == MoveCandidate.CandidateMoveType.SPAWN_FROM_BASE) {
                spawnMove = m;
            }
        }

        if (blockCreatingMove != null) {
            return MoveDecision.make(blockCreatingMove);
        }

        if (spawnMove != null) {
            return MoveDecision.make(spawnMove);
        }

        List<MoveCandidate> homeboundMoves = new ArrayList<>();
        for (MoveCandidate m : legalMoves) {
            if (m.moveType() == MoveCandidate.CandidateMoveType.REACH_HOME ||
                m.moveType() == MoveCandidate.CandidateMoveType.ENTER_HOME_STRAIGHT ||
                m.moveType() == MoveCandidate.CandidateMoveType.HOME_STRAIGHT_ADVANCE) {
                homeboundMoves.add(m);
            }
        }
        if (!homeboundMoves.isEmpty()) {
            homeboundMoves.sort(Comparator.comparingInt(MoveCandidate::distanceToHomeAfterMove));
            return MoveDecision.make(homeboundMoves.get(0));
        }

        for (MoveCandidate m : legalMoves) {
            if (m.isMovingBlockade()) {
                return MoveDecision.make(m);
            }
        }

        List<MoveCandidate> nonBlockBreakingMoves = new ArrayList<>();
        for (MoveCandidate m : legalMoves) {
            if (!m.isBreakingBlockade()) {
                nonBlockBreakingMoves.add(m);
            }
        }

        if (!nonBlockBreakingMoves.isEmpty()) {

            nonBlockBreakingMoves.sort(Comparator.comparingInt(MoveCandidate::distanceToHomeAfterMove));
            return MoveDecision.make(nonBlockBreakingMoves.get(0));
        }

        legalMoves.sort(Comparator.comparingInt(MoveCandidate::distanceToHomeAfterMove));
        return MoveDecision.make(legalMoves.get(0));
    }

    @Override
    public String getStrategyName() {
        return "GreenBlockerStrategy";
    }
}
