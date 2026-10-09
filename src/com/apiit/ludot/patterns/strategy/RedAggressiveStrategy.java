package com.apiit.ludot.patterns.strategy;

import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.patterns.nullobject.NullMove;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.dto.MoveCandidate;
import com.apiit.ludot.usecases.dto.MoveDecision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RedAggressiveStrategy implements PlayerStrategy {

    @Override
    public MoveDecision decideMove(Player player, List<MoveCandidate> legalMoves,
                                   GameStateDTO gameState, BoardCoordinateCalculator calculator) {
        if (legalMoves == null || legalMoves.isEmpty()) {
            return NullMove.getInstance();
        }

        List<MoveCandidate> capturingMoves = new ArrayList<>();
        for (MoveCandidate m : legalMoves) {
            if (m.isCapturingOpponent() || m.isCapturingBlockade()) {
                capturingMoves.add(m);
            }
        }

        if (!capturingMoves.isEmpty()) {

            capturingMoves.sort(Comparator.comparingInt(m -> {
                if (m.opponentPieceToCapture() != null) {
                    return calculator.calculateDistanceToHome(m.opponentPieceToCapture());
                }
                return Integer.MAX_VALUE;
            }));
            return MoveDecision.make(capturingMoves.get(0));
        }

        List<Piece> piecesOnBoard = player.getPiecesOnBoard();
        MoveCandidate spawnMove = findSpawnMove(legalMoves);

        if (spawnMove != null) {
            if (piecesOnBoard.isEmpty()) {

                return MoveDecision.make(spawnMove);
            } else {

                return MoveDecision.make(spawnMove);
            }
        }

        List<MoveCandidate> nonBlockMoves = new ArrayList<>();
        for (MoveCandidate m : legalMoves) {
            if (!m.isCreatingBlockade()) {
                nonBlockMoves.add(m);
            }
        }

        List<MoveCandidate> viableMoves = !nonBlockMoves.isEmpty() ? nonBlockMoves : legalMoves;

        viableMoves.sort(Comparator.comparingInt(MoveCandidate::distanceToHomeAfterMove));
        return MoveDecision.make(viableMoves.get(0));
    }

    private MoveCandidate findSpawnMove(List<MoveCandidate> moves) {
        for (MoveCandidate m : moves) {
            if (m.moveType() == MoveCandidate.CandidateMoveType.SPAWN_FROM_BASE) {
                return m;
            }
        }
        return null;
    }

    @Override
    public String getStrategyName() {
        return "RedAggressiveStrategy";
    }
}
