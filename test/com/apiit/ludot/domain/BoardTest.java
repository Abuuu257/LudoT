package com.apiit.ludot.domain;

import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Cell;
import com.apiit.ludot.domain.model.CellType;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Board Entity Tests")
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    @DisplayName("Board has 52 track cells after initialization")
    void boardHas52TrackCells() {
        assertEquals(52, board.getAllTrackCells().size(),
                "Standard Ludo board must have exactly 52 track cells");
    }

    @Test
    @DisplayName("Track cell 26 is Red's starting X")
    void redStartingXAtCell26() {
        Cell cell = board.getTrackCell(26);
        assertEquals(CellType.STARTING_X, cell.getCellType(),
                "Cell 26 must be a STARTING_X type");
        assertEquals(Color.RED, cell.getAssociatedColor(),
                "Cell 26 is Red's starting X");
    }

    @Test
    @DisplayName("Track cell 0 is Yellow's starting X")
    void yellowStartingXAtCell0() {
        Cell cell = board.getTrackCell(0);
        assertEquals(CellType.STARTING_X, cell.getCellType(),
                "Cell 0 must be Yellow's STARTING_X");
        assertEquals(Color.YELLOW, cell.getAssociatedColor());
    }

    @Test
    @DisplayName("Track cell 13 is Blue's starting X")
    void blueStartingXAtCell13() {
        Cell cell = board.getTrackCell(13);
        assertEquals(CellType.STARTING_X, cell.getCellType());
        assertEquals(Color.BLUE, cell.getAssociatedColor());
    }

    @Test
    @DisplayName("Track cell 39 is Green's starting X")
    void greenStartingXAtCell39() {
        Cell cell = board.getTrackCell(39);
        assertEquals(CellType.STARTING_X, cell.getCellType());
        assertEquals(Color.GREEN, cell.getAssociatedColor());
    }

    @Test
    @DisplayName("Approach cells are correctly assigned")
    void approachCellsCorrectlyAssigned() {
        assertEquals(CellType.APPROACH, board.getTrackCell(25).getCellType(),
                "Cell 25 must be Red's approach");
        assertEquals(Color.RED, board.getTrackCell(25).getAssociatedColor());
        assertEquals(CellType.APPROACH, board.getTrackCell(51).getCellType(),
                "Cell 51 must be Yellow's approach");
    }

    @Test
    @DisplayName("Track index wraps around at 52 correctly")
    void trackIndexWrapsAround() {
        Cell cell52 = board.getTrackCell(52);
        Cell cell0 = board.getTrackCell(0);
        assertEquals(cell0.getCellId(), cell52.getCellId(),
                "Index 52 must wrap to 0");
    }

    @Test
    @DisplayName("Home straight has 5 cells per colour")
    void homeStraightHasFiveCells() {
        for (Color color : Color.values()) {
            for (int i = 0; i < 5; i++) {
                Cell cell = board.getHomeStraightCell(color, i);
                assertNotNull(cell, color + " home straight cell " + i + " must exist");
                assertEquals(CellType.HOME_STRAIGHT, cell.getCellType());
                assertEquals(color, cell.getAssociatedColor());
            }
        }
    }

    @Test
    @DisplayName("Home straight cell ID matches naming convention")
    void homeStraightCellNamingConvention() {
        Cell cell = board.getHomeStraightCell(Color.RED, 2);
        assertEquals("redhomepath2", cell.getCellId(),
                "Home straight cell ID should be 'redhomepath2'");
    }

    @Test
    @DisplayName("Mystery cell not active at game start")
    void mysteryCellNotActiveAtStart() {
        assertFalse(board.isMysteryCellActive(),
                "Mystery cell must not be active at game start");
        assertEquals(-1, board.getMysteryCellLocation(),
                "Mystery cell location should be -1 when inactive");
    }

    @Test
    @DisplayName("Manually spawned mystery cell is active")
    void manuallySpawnedMysteryCellIsActive() {
        board.setMysteryCellManually(10, 4);
        assertTrue(board.isMysteryCellActive(), "Mystery cell should now be active");
        assertEquals(10, board.getMysteryCellLocation());
        assertEquals(4, board.getMysteryRoundsRemaining());
        assertTrue(board.getTrackCell(10).isMysteryCell(),
                "Track cell 10 should be flagged as mystery cell");
    }

    @Test
    @DisplayName("Despawning mystery cell clears it from track cell")
    void despawnMysteryCellClearsFlag() {
        board.setMysteryCellManually(10, 4);
        board.despawnMysteryCell();

        assertFalse(board.isMysteryCellActive());
        assertFalse(board.getTrackCell(10).isMysteryCell(),
                "Track cell should no longer be flagged after despawn");
    }

    @Test
    @DisplayName("Cell blockade detected when two same-colour pieces occupy cell")
    void blockadeDetectedWithTwoPiecesOfSameColour() {
        Piece p1 = new Piece(Color.RED, 1);
        Piece p2 = new Piece(Color.RED, 2);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);

        Cell cell = board.getTrackCell(26);
        cell.addOccupant(p1);
        cell.addOccupant(p2);

        assertTrue(cell.isBlockade(), "Two same-colour pieces form a blockade");
        assertEquals(Color.RED, cell.getBlockadeOwner());
    }

    @Test
    @DisplayName("Cell with mixed colours is not a blockade")
    void mixedColourCellIsNotBlockade() {
        Piece red = new Piece(Color.RED, 1);
        Piece green = new Piece(Color.GREEN, 1);
        red.spawnToStart(Direction.CLOCKWISE);
        green.spawnToStart(Direction.CLOCKWISE);

        Cell cell = board.getTrackCell(5);
        cell.addOccupant(red);
        cell.addOccupant(green);

        assertFalse(cell.isBlockade(),
                "Pieces of different colours on same cell are NOT a blockade");
    }

    @Test
    @DisplayName("Single piece does not form a blockade")
    void singlePieceIsNotBlockade() {
        Piece p = new Piece(Color.YELLOW, 1);
        p.spawnToStart(Direction.CLOCKWISE);

        Cell cell = board.getTrackCell(0);
        cell.addOccupant(p);

        assertFalse(cell.isBlockade(), "A single piece cannot form a blockade");
    }

    @Test
    @DisplayName("Board detects no pieces on track initially")
    void boardHasNoPiecesOnTrackInitially() {
        assertFalse(board.hasAnyPieceOnTrack(),
                "No pieces should be on the track at game start");
    }

    @Test
    @DisplayName("Board detects pieces after one is placed")
    void boardDetectsPiecesOnTrackAfterPlacement() {
        Piece p = new Piece(Color.BLUE, 1);
        p.spawnToStart(Direction.CLOCKWISE);
        board.getTrackCell(13).addOccupant(p);

        assertTrue(board.hasAnyPieceOnTrack(),
                "Board should detect the Blue piece on track");
    }

    @Test
    @DisplayName("getBlockadeAt returns null when no blockade")
    void getBlockadeAtReturnsNullWhenNone() {
        assertNull(board.getBlockadeAt(10),
                "No blockade at empty cell");
    }

    @Test
    @DisplayName("getBlockadeAt returns Blockade when blockade exists")
    void getBlockadeAtReturnBlockadeObject() {
        Piece p1 = new Piece(Color.GREEN, 1);
        Piece p2 = new Piece(Color.GREEN, 2);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);

        Cell cell = board.getTrackCell(39);
        cell.addOccupant(p1);
        cell.addOccupant(p2);

        Blockade blockade = board.getBlockadeAt(39);
        assertNotNull(blockade, "Blockade should be detected");
        assertEquals(Color.GREEN, blockade.getOwnerColor());
        assertEquals(2, blockade.size());
    }
}
