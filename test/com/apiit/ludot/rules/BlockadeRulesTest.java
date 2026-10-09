package com.apiit.ludot.rules;

import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Cell;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Blockade Rules Tests")
class BlockadeRulesTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    @DisplayName("Two pieces of same colour on same cell form blockade (Rule T-3)")
    void twoSameColourPiecesFormBlockade() {
        Piece p1 = new Piece(Color.RED, 1);
        Piece p2 = new Piece(Color.RED, 2);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);

        Cell cell = board.getTrackCell(5);
        cell.addOccupant(p1);
        cell.addOccupant(p2);

        assertTrue(cell.isBlockade(), "Two same-colour pieces must form a blockade");
        assertEquals(Color.RED, cell.getBlockadeOwner());
    }

    @Test
    @DisplayName("Three pieces of same colour on same cell also form blockade")
    void threePiecesFormBlockade() {
        Piece p1 = new Piece(Color.GREEN, 1);
        Piece p2 = new Piece(Color.GREEN, 2);
        Piece p3 = new Piece(Color.GREEN, 3);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);
        p3.spawnToStart(Direction.CLOCKWISE);

        Cell cell = board.getTrackCell(10);
        cell.addOccupant(p1);
        cell.addOccupant(p2);
        cell.addOccupant(p3);

        assertTrue(cell.isBlockade());
        assertEquals(3, cell.getOccupantCount());
    }

    @Test
    @DisplayName("Block move distance is dice / block size (Rule T-4)")
    void blockMoveDistanceDividedBySize() {
        Piece p1 = new Piece(Color.BLUE, 1);
        Piece p2 = new Piece(Color.BLUE, 2);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);

        java.util.List<Piece> pieces = java.util.List.of(p1, p2);
        Blockade blockade = new Blockade(Color.BLUE, 13, pieces);

        int moveDistance = blockade.calculateBlockMoveDistance(6);
        assertEquals(3, moveDistance, "6 dice / 2 pieces = 3 steps (Rule T-4)");
    }

    @Test
    @DisplayName("Block move distance with 3 pieces and 6 dice is 2")
    void blockMoveDistanceThreePieces() {
        Piece p1 = new Piece(Color.RED, 1);
        Piece p2 = new Piece(Color.RED, 2);
        Piece p3 = new Piece(Color.RED, 3);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);
        p3.spawnToStart(Direction.CLOCKWISE);

        java.util.List<Piece> pieces = java.util.List.of(p1, p2, p3);
        Blockade blockade = new Blockade(Color.RED, 26, pieces);

        int moveDistance = blockade.calculateBlockMoveDistance(6);
        assertEquals(2, moveDistance, "6 dice / 3 pieces = 2 steps");
    }

    @Test
    @DisplayName("Blockade of same size can capture opponent blockade (Rule T-8)")
    void sameBlockadeSizeCanCapture() {
        Piece p1 = new Piece(Color.RED, 1);
        Piece p2 = new Piece(Color.RED, 2);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);

        Piece g1 = new Piece(Color.GREEN, 1);
        Piece g2 = new Piece(Color.GREEN, 2);
        g1.spawnToStart(Direction.CLOCKWISE);
        g2.spawnToStart(Direction.CLOCKWISE);

        Blockade attackingBlockade = new Blockade(Color.RED, 26, java.util.List.of(p1, p2));
        Blockade targetBlockade = new Blockade(Color.GREEN, 30, java.util.List.of(g1, g2));

        assertTrue(attackingBlockade.canCapture(targetBlockade),
                "Same-size blockades can capture each other (Rule T-8)");
    }

    @Test
    @DisplayName("Larger blockade cannot capture smaller blockade (Rule T-8)")
    void largerBlockadeCannotCaptureSmallerBlockade() {
        Piece p1 = new Piece(Color.RED, 1);
        Piece p2 = new Piece(Color.RED, 2);
        Piece p3 = new Piece(Color.RED, 3);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);
        p3.spawnToStart(Direction.CLOCKWISE);

        Piece g1 = new Piece(Color.GREEN, 1);
        Piece g2 = new Piece(Color.GREEN, 2);
        g1.spawnToStart(Direction.CLOCKWISE);
        g2.spawnToStart(Direction.CLOCKWISE);

        Blockade bigBlockade = new Blockade(Color.RED, 26, java.util.List.of(p1, p2, p3));
        Blockade smallBlockade = new Blockade(Color.GREEN, 30, java.util.List.of(g1, g2));

        assertFalse(bigBlockade.canCapture(smallBlockade),
                "Blockade of size 3 cannot capture blockade of size 2 (Rule T-8)");
    }

    @Test
    @DisplayName("Blockade cannot capture own colour")
    void blockadeCannotCaptureSameColour() {
        Piece p1 = new Piece(Color.RED, 1);
        Piece p2 = new Piece(Color.RED, 2);
        Piece p3 = new Piece(Color.RED, 3);
        Piece p4 = new Piece(Color.RED, 4);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);
        p3.spawnToStart(Direction.CLOCKWISE);
        p4.spawnToStart(Direction.CLOCKWISE);

        Blockade b1 = new Blockade(Color.RED, 26, java.util.List.of(p1, p2));
        Blockade b2 = new Blockade(Color.RED, 30, java.util.List.of(p3, p4));

        assertFalse(b1.canCapture(b2), "Own blockade cannot capture own blockade");
    }

    @Test
    @DisplayName("Blockade direction determined by longest distance to home when pieces go opposite ways")
    void blockadeDirectionDeterminedByLongestDistance() {
        Piece p1 = new Piece(Color.YELLOW, 1);
        Piece p2 = new Piece(Color.YELLOW, 2);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.COUNTER_CLOCKWISE);

        Blockade blockade = new Blockade(Color.YELLOW, 0, java.util.List.of(p1, p2));

        Direction blockDir = blockade.determineBlockDirection(100, 5);
        assertEquals(Direction.CLOCKWISE, blockDir,
                "Block moves in direction of the longest distance from home");
    }

    @Test
    @DisplayName("Creating Blockade with fewer than 2 pieces throws exception")
    void creatingBlockadeWithOnePieceThrowsException() {
        Piece p = new Piece(Color.RED, 1);
        p.spawnToStart(Direction.CLOCKWISE);

        assertThrows(IllegalArgumentException.class,
                () -> new Blockade(Color.RED, 26, java.util.List.of(p)),
                "Blockade requires at least 2 pieces");
    }
}
