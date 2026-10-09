package com.apiit.ludot.domain.service;

import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.patterns.singleton.BoardRegistry;

public class BoardCoordinateCalculator {

    private final BoardRegistry registry;

    public BoardCoordinateCalculator() {
        this.registry = BoardRegistry.getInstance();
    }

    public int normalizeTrackIndex(int index) {
        int mod = index % BoardRegistry.TRACK_SIZE;
        return mod < 0 ? mod + BoardRegistry.TRACK_SIZE : mod;
    }

    public int calculateDistanceOnTrack(int from, int to, Direction direction) {
        return registry.calculateTrackDistance(from, to, direction == Direction.CLOCKWISE);
    }

    public int calculateDistanceToHome(Piece piece) {
        if (piece.getState().isInHome()) {
            return 0;
        }
        if (piece.getState().isAtBase()) {
            return 57;
        }
        if (piece.getState().isInHomeStraight()) {

            return BoardRegistry.HOME_STRAIGHT_SIZE - piece.getHomeStraightIndex();
        }

        int trackIndex = piece.getTrackIndex();
        int approachIndex = piece.getColor().getApproachIndex();
        Direction dir = piece.getCurrentDirection();

        int stepsToApproach = calculateDistanceOnTrack(trackIndex, approachIndex, dir);

        if (dir == Direction.COUNTER_CLOCKWISE && piece.getApproachPassCount() < 1) {
            stepsToApproach += BoardRegistry.TRACK_SIZE;
        }

        return stepsToApproach + 6;
    }

    public int calculateTrackCellDistanceToHome(Color color, int trackIndex, Direction dir) {
        int approach = color.getApproachIndex();
        int stepsToApproach = calculateDistanceOnTrack(trackIndex, approach, dir);
        return stepsToApproach + 6;
    }

    public PathEvaluationResult evaluateMove(Piece piece, int units, Board board) {
        if (!piece.getState().canMove() && !piece.getState().isInHomeStraight()) {
            return PathEvaluationResult.illegal("Piece cannot move in its current state");
        }

        if (units <= 0) {
            return PathEvaluationResult.illegal("Move units must be positive");
        }

        if (piece.getState().isInHomeStraight()) {
            int currentStraight = piece.getHomeStraightIndex();
            int targetStraight = currentStraight + units;

            if (targetStraight < BoardRegistry.HOME_STRAIGHT_SIZE) {

                return PathEvaluationResult.validHomeStraightMove(
                        piece.getCurrentCellId(),
                        piece.getColor().getHomePathCellName(targetStraight),
                        targetStraight,
                        false
                );
            } else if (targetStraight == BoardRegistry.HOME_STRAIGHT_SIZE) {

                return PathEvaluationResult.validHomeStraightMove(
                        piece.getCurrentCellId(),
                        "Home",
                        5,
                        true
                );
            } else {

                return PathEvaluationResult.illegal("Overshoots Home: exact roll required");
            }
        }

        int currentTrackIndex = piece.getTrackIndex();
        Direction dir = piece.getCurrentDirection();
        int delta = dir.getStepDelta();
        int approachIndex = piece.getColor().getApproachIndex();

        int cursor = currentTrackIndex;
        boolean enteredHomeStraight = false;
        int homeStraightIndex = -1;
        boolean reachedHome = false;
        int passCount = piece.getApproachPassCount();
        boolean passedApproachThisTurn = false;

        for (int step = 1; step <= units; step++) {
            if (enteredHomeStraight) {
                homeStraightIndex++;
                if (homeStraightIndex == BoardRegistry.HOME_STRAIGHT_SIZE) {
                    reachedHome = true;

                    if (step < units) {
                        return PathEvaluationResult.illegal("Overshoots Home: exact roll required");
                    }
                } else if (homeStraightIndex > BoardRegistry.HOME_STRAIGHT_SIZE) {
                    return PathEvaluationResult.illegal("Overshoots Home: exact roll required");
                }
            } else {
                int nextTrackIndex = normalizeTrackIndex(cursor + delta);

                Blockade blockade = board.getBlockadeAt(nextTrackIndex);
                if (blockade != null && blockade.getOwnerColor() != piece.getColor()) {

                    int cellBeforeBlock = cursor;
                    return PathEvaluationResult.blocked(
                            piece.getCurrentCellId(),
                            String.valueOf(normalizeTrackIndex(currentTrackIndex + (units * delta))),
                            String.valueOf(cellBeforeBlock),
                            cellBeforeBlock,
                            blockade
                    );
                }

                boolean isApproachCell = (nextTrackIndex == approachIndex);
                if (isApproachCell) {
                    passCount++;
                    passedApproachThisTurn = true;
                }

                boolean canEnter = piece.getCaptureCount() >= 1;
                if (dir == Direction.COUNTER_CLOCKWISE) {
                    canEnter = canEnter && (passCount >= 2);
                }

                if (cursor == approachIndex && canEnter) {
                    enteredHomeStraight = true;
                    homeStraightIndex = 0;
                } else {
                    cursor = nextTrackIndex;
                }
            }
        }

        if (reachedHome) {
            return PathEvaluationResult.validReachHome(piece.getCurrentCellId(), "Home", passedApproachThisTurn);
        }

        if (enteredHomeStraight) {
            String targetCellId = piece.getColor().getHomePathCellName(homeStraightIndex);
            return PathEvaluationResult.validHomeStraightEntry(
                    piece.getCurrentCellId(),
                    targetCellId,
                    homeStraightIndex,
                    passedApproachThisTurn
            );
        }

        String targetCellId = String.valueOf(cursor);
        return PathEvaluationResult.validTrackMove(
                piece.getCurrentCellId(),
                targetCellId,
                cursor,
                passedApproachThisTurn
        );
    }
}
