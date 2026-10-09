package com.apiit.ludot.patterns.strategy;

import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.patterns.nullobject.NullMove;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.dto.MoveCandidate;
import com.apiit.ludot.usecases.dto.MoveDecision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlueMysteryStrategy implements PlayerStrategy {

    private int nextCyclicPieceNumber = 1;

    public int getNextCyclicPieceNumber() {
        return nextCyclicPieceNumber;
    }

    public void setNextCyclicPieceNumber(int nextCyclicPieceNumber) {
        this.nextCyclicPieceNumber = nextCyclicPieceNumber;
    }

    @Override
    public MoveDecision decideMove(Player player, List<MoveCandidate> legalMoves,
                                   GameStateDTO gameState, BoardCoordinateCalculator calculator) {
        if (legalMoves == null || legalMoves.isEmpty()) {
            return NullMove.getInstance();
        }

        for (int offset = 0; offset < 4; offset++) {
            int targetPieceNum = ((nextCyclicPieceNumber - 1 + offset) % 4) + 1;
            List<MoveCandidate> pieceMoves = filterMovesForPieceNumber(legalMoves, targetPieceNum);

            if (!pieceMoves.isEmpty()) {
                MoveCandidate chosen = selectMoveForBluePiece(pieceMoves);

                this.nextCyclicPieceNumber = (targetPieceNum % 4) + 1;
                return MoveDecision.make(chosen);
            }
        }

        MoveCandidate chosen = selectMoveForBluePiece(legalMoves);
        this.nextCyclicPieceNumber = (chosen.piece().getPieceNumber() % 4) + 1;
        return MoveDecision.make(chosen);
    }

    private MoveCandidate selectMoveForBluePiece(List<MoveCandidate> moves) {
        if (moves.size() == 1) {
            return moves.get(0);
        }

        MoveCandidate first = moves.get(0);
        Direction dir = first.piece().getCurrentDirection();

        if (dir == Direction.COUNTER_CLOCKWISE) {

            for (MoveCandidate m : moves) {
                if (m.isLandingOnMystery()) {
                    return m;
                }
            }
        } else {

            List<MoveCandidate> nonMysteryMoves = new ArrayList<>();
            for (MoveCandidate m : moves) {
                if (!m.isLandingOnMystery()) {
                    nonMysteryMoves.add(m);
                }
            }
            if (!nonMysteryMoves.isEmpty()) {
                nonMysteryMoves.sort(Comparator.comparingInt(MoveCandidate::distanceToHomeAfterMove));
                return nonMysteryMoves.get(0);
            }
        }

        moves.sort(Comparator.comparingInt(MoveCandidate::distanceToHomeAfterMove));
        return moves.get(0);
    }

    private List<MoveCandidate> filterMovesForPieceNumber(List<MoveCandidate> moves, int pieceNum) {
        List<MoveCandidate> filtered = new ArrayList<>();
        for (MoveCandidate m : moves) {
            if (m.piece().getPieceNumber() == pieceNum) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    @Override
    public String getStrategyName() {
        return "BlueMysteryStrategy";
    }
}
