package com.apiit.ludot.rules;

import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.domain.service.PathEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Movement Rules Tests")
class MovementRulesTest {

    private Board board;
    private BoardCoordinateCalculator calculator;

    @BeforeEach
    void setUp() {
        board = new Board();
        calculator = new BoardCoordinateCalculator();
    }

    @Test
    @DisplayName("Clockwise movement from cell 0 by 5 steps reaches cell 5")
    void clockwiseMovement5Steps() {
        Piece yellow = new Piece(Color.YELLOW, 1);
        yellow.spawnToStart(Direction.CLOCKWISE);
        board.getTrackCell(0).addOccupant(yellow);

        PathEvaluationResult result = calculator.evaluateMove(yellow, 5, board);

        assertTrue(result.isValid(), "Move should be valid");
        assertEquals(5, result.getTargetTrackIndex(), "Should land on cell 5");
    }

    @Test
    @DisplayName("Counter-clockwise movement from cell 10 by 3 steps reaches cell 7")
    void counterClockwiseMovement3Steps() {
        Piece piece = new Piece(Color.RED, 1);
        piece.spawnToStart(Direction.COUNTER_CLOCKWISE);
        board.getTrackCell(26).removeOccupant(piece);
        piece.moveToTrackCell(10);
        board.getTrackCell(10).addOccupant(piece);

        PathEvaluationResult result = calculator.evaluateMove(piece, 3, board);

        assertTrue(result.isValid());
        assertEquals(7, result.getTargetTrackIndex(),
                "Counter-clockwise from 10 by 3 = cell 7");
    }

    @Test
    @DisplayName("Clockwise wrap-around from cell 50 by 4 steps reaches cell 2")
    void clockwiseWrapAround() {
        Piece piece = new Piece(Color.BLUE, 2);
        piece.spawnToStart(Direction.CLOCKWISE);
        piece.moveToTrackCell(50);
        board.getTrackCell(50).addOccupant(piece);

        PathEvaluationResult result = calculator.evaluateMove(piece, 4, board);

        assertTrue(result.isValid());
        assertEquals(2, result.getTargetTrackIndex(),
                "Wrap from 50+4 mod 52 = 2");
    }

    @Test
    @DisplayName("Piece blocked by opponent blockade stops before it (Rule T-3)")
    void pieceBlockedByOpponentBlockade() {
        Piece movingPiece = new Piece(Color.YELLOW, 1);
        movingPiece.spawnToStart(Direction.CLOCKWISE);
        board.getTrackCell(0).addOccupant(movingPiece);

        Piece blocker1 = new Piece(Color.RED, 1);
        Piece blocker2 = new Piece(Color.RED, 2);
        blocker1.spawnToStart(Direction.CLOCKWISE);
        blocker2.spawnToStart(Direction.CLOCKWISE);
        board.getTrackCell(4).addOccupant(blocker1);
        board.getTrackCell(4).addOccupant(blocker2);

        PathEvaluationResult result = calculator.evaluateMove(movingPiece, 6, board);

        assertTrue(result.isBlocked(), "Movement should be blocked");
        assertEquals(3, result.getCellBeforeBlockTrackIndex(),
                "Should stop at cell before blockade (cell 3)");
    }

    @Test
    @DisplayName("Piece cannot enter home straight without capture (Rule T-7)")
    void pieceCannotEnterHomeStraightWithoutCapture() {
        Piece piece = new Piece(Color.RED, 1);
        piece.spawnToStart(Direction.CLOCKWISE);
        piece.moveToTrackCell(25);
        board.getTrackCell(25).addOccupant(piece);

        PathEvaluationResult result = calculator.evaluateMove(piece, 1, board);

        assertFalse(result.isEntersHomeStraight(),
                "Without a capture, piece should not enter home straight (Rule T-7)");
    }

    @Test
    @DisplayName("Piece enters home straight when at approach with 1 capture clockwise")
    void pieceEntersHomeStraightClockwiseWithCapture() {
        Piece piece = new Piece(Color.RED, 1);
        piece.spawnToStart(Direction.CLOCKWISE);
        piece.moveToTrackCell(25);
        piece.incrementCaptureCount();
        piece.incrementApproachPassCount();
        board.getTrackCell(25).addOccupant(piece);

        PathEvaluationResult result = calculator.evaluateMove(piece, 1, board);

        assertTrue(result.isEntersHomeStraight(),
                "With 1 capture clockwise at approach, piece should enter home straight");
    }

    @Test
    @DisplayName("Piece reaching Home with exact roll (Rule 10)")
    void pieceReachesHomeWithExactRoll() {
        Piece piece = new Piece(Color.RED, 1);
        piece.spawnToStart(Direction.CLOCKWISE);
        piece.enterHomeStraight(3);

        PathEvaluationResult result = calculator.evaluateMove(piece, 2, board);

        assertTrue(result.isReachesHome(),
                "Rolling 2 from home straight index 3 should reach Home exactly (3+2=5=Home)");
    }

    @Test
    @DisplayName("Overshoot home straight is illegal (Rule 10)")
    void overshootHomeStraightIsIllegal() {
        Piece piece = new Piece(Color.RED, 1);
        piece.spawnToStart(Direction.CLOCKWISE);
        piece.enterHomeStraight(3);

        PathEvaluationResult result = calculator.evaluateMove(piece, 5, board);

        assertFalse(result.isValid(),
                "Overshooting home (3+5=8 > 5) must be illegal (Rule 10)");
        assertNotNull(result.getFailureReason(),
                "Failure reason should be provided");
    }

    @Test
    @DisplayName("Piece in base cannot be evaluated for movement")
    void pieceInBaseCannotBeEvaluated() {
        Piece piece = new Piece(Color.GREEN, 1);

        PathEvaluationResult result = calculator.evaluateMove(piece, 4, board);

        assertFalse(result.isValid(),
                "Piece in base state should produce invalid path result");
    }

    @Test
    @DisplayName("Counter-clockwise wrap from cell 2 by 4 steps reaches cell 50")
    void counterClockwiseWrapAround() {
        Piece piece = new Piece(Color.BLUE, 1);
        piece.spawnToStart(Direction.COUNTER_CLOCKWISE);
        piece.moveToTrackCell(2);
        board.getTrackCell(2).addOccupant(piece);

        PathEvaluationResult result = calculator.evaluateMove(piece, 4, board);

        assertTrue(result.isValid());
        assertEquals(50, result.getTargetTrackIndex(),
                "Counter-clockwise from 2 by 4 = 52-4+2 = 50 mod 52");
    }

    @Test
    @DisplayName("Distance calculation clockwise from 0 to 10 is 10")
    void distanceCalculationClockwise() {
        int dist = calculator.calculateDistanceOnTrack(0, 10, Direction.CLOCKWISE);
        assertEquals(10, dist);
    }

    @Test
    @DisplayName("Distance calculation counter-clockwise from 10 to 0 is 10")
    void distanceCalculationCounterClockwise() {
        int dist = calculator.calculateDistanceOnTrack(10, 0, Direction.COUNTER_CLOCKWISE);
        assertEquals(10, dist);
    }

    @Test
    @DisplayName("Normalize track index works for negative numbers")
    void normalizeTrackIndexHandlesNegatives() {
        int normalized = calculator.normalizeTrackIndex(-1);
        assertEquals(51, normalized, "Normalising -1 should yield 51");
    }
}
