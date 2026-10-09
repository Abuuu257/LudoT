package com.apiit.ludot.usecases.dto;

import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.service.PathEvaluationResult;

public record MoveCandidate(
        Piece piece,
        CandidateMoveType moveType,
        int effectiveUnits,
        PathEvaluationResult pathResult,
        boolean isCapturingOpponent,
        Piece opponentPieceToCapture,
        boolean isCapturingBlockade,
        Blockade opponentBlockadeToCapture,
        boolean isCreatingBlockade,
        boolean isMovingBlockade,
        Blockade participatingBlockade,
        boolean isBreakingBlockade,
        boolean isLandingOnMystery,
        int distanceToHomeAfterMove
) {
    public enum CandidateMoveType {
        SPAWN_FROM_BASE,
        TRACK_MOVE,
        BLOCKED_MOVE_TO_ADJACENT,
        ENTER_HOME_STRAIGHT,
        HOME_STRAIGHT_ADVANCE,
        REACH_HOME,
        MOVE_BLOCKADE,
        BREAK_BLOCKADE
    }
}
