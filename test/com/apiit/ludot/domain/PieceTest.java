package com.apiit.ludot.domain;

import com.apiit.ludot.domain.model.AuraEffect;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Piece Entity Tests")
class PieceTest {

    private Piece redPiece;

    @BeforeEach
    void setUp() {
        redPiece = new Piece(Color.RED, 1);
    }

    @Test
    @DisplayName("New piece starts in Base state")
    void newPieceShouldStartInBaseState() {
        assertTrue(redPiece.getState().isAtBase(),
                "Freshly created piece must be in Base state");
        assertEquals("R1", redPiece.getId(),
                "Piece ID should be colour prefix + number");
    }

    @Test
    @DisplayName("Piece in base cannot move")
    void pieceInBaseCannotMove() {
        assertFalse(redPiece.getState().canMove(),
                "Piece in Base must not be movable");
    }

    @Test
    @DisplayName("Spawning a piece moves it to starting X with given direction")
    void spawnPieceSetsDirectionAndTrackIndex() {
        redPiece.spawnToStart(Direction.CLOCKWISE);

        assertTrue(redPiece.getState().isOnTrack(),
                "After spawn, piece should be on track");
        assertEquals(Color.RED.getStartXIndex(), redPiece.getTrackIndex(),
                "Track index should match Red's starting X (26)");
        assertEquals(Direction.CLOCKWISE, redPiece.getCurrentDirection(),
                "Direction should be set from coin toss");
    }

    @Test
    @DisplayName("Spawning counter-clockwise sets counter-clockwise direction")
    void spawnWithCounterClockwiseDirectionWorks() {
        redPiece.spawnToStart(Direction.COUNTER_CLOCKWISE);
        assertEquals(Direction.COUNTER_CLOCKWISE, redPiece.getCurrentDirection());
    }

    @Test
    @DisplayName("Moving to home straight changes state to HomeStraight")
    void pieceEntersHomeStraightState() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.enterHomeStraight(2);

        assertTrue(redPiece.getState().isInHomeStraight(),
                "Piece should be in HomeStraight state");
        assertEquals(2, redPiece.getHomeStraightIndex(),
                "Home straight index should be 2");
        assertFalse(redPiece.getState().isOnTrack(),
                "Piece in home straight is not on track");
    }

    @Test
    @DisplayName("Reaching Home transitions piece to HomeState")
    void pieceEntersHomeState() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.enterHome();

        assertTrue(redPiece.getState().isInHome(),
                "Piece should be in Home state");
        assertFalse(redPiece.getState().canMove(),
                "Piece in Home cannot move");
        assertFalse(redPiece.getState().isOnTrack(),
                "Piece in Home is not on track");
    }

    @Test
    @DisplayName("Reset to base restores all fields to initial values")
    void resetToBaseRestoresAllFields() {
        redPiece.spawnToStart(Direction.COUNTER_CLOCKWISE);
        redPiece.incrementCaptureCount();
        redPiece.applyAlphaEffect(AuraEffect.ENERGIZED);
        redPiece.resetToBase();

        assertTrue(redPiece.getState().isAtBase(), "Must be back in Base");
        assertEquals(0, redPiece.getCaptureCount(), "Capture count resets to 0");
        assertEquals(AuraEffect.NONE, redPiece.getAuraEffect(), "Aura effect resets");
        assertEquals(-1, redPiece.getTrackIndex(), "Track index resets to -1");
    }

    @Test
    @DisplayName("Energized aura doubles effective movement")
    void energizedAuraDoublesMovement() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.applyAlphaEffect(AuraEffect.ENERGIZED);

        int effective = redPiece.calculateEffectiveMovement(3);
        assertEquals(6, effective, "Energized aura must double movement (3 -> 6)");
    }

    @Test
    @DisplayName("Sick aura halves effective movement (min 1)")
    void sickAuraHalvesMovement() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.applyAlphaEffect(AuraEffect.SICK);

        assertEquals(1, redPiece.calculateEffectiveMovement(2),
                "Sick aura halves movement (2 -> 1, minimum 1)");
        assertEquals(2, redPiece.calculateEffectiveMovement(5),
                "Sick aura halves movement (5 -> 2)");
    }

    @Test
    @DisplayName("Aura effect expires after 4 rounds")
    void auraEffectExpiresAfterFourRounds() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.applyAlphaEffect(AuraEffect.ENERGIZED);
        assertEquals(4, redPiece.getAuraRoundsRemaining());

        for (int i = 0; i < 4; i++) {
            redPiece.getState().onRoundEnd(redPiece);
        }

        assertEquals(AuraEffect.NONE, redPiece.getAuraEffect(),
                "Aura should expire after 4 rounds");
        assertEquals(0, redPiece.getAuraRoundsRemaining());
    }

    @Test
    @DisplayName("Briefing state prevents movement")
    void briefingStatePreventsMovement() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.attendBriefing(26);

        assertFalse(redPiece.getState().canMove(),
                "Piece in briefing cannot move");
        assertTrue(redPiece.getState().isRestricted(),
                "Piece in briefing is restricted");
        assertEquals(4, redPiece.getBriefingRoundsRemaining(),
                "Briefing lasts 4 rounds");
    }

    @Test
    @DisplayName("Briefing expires after 4 decrements, piece can move again")
    void briefingExpiresAfterFourRounds() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.attendBriefing(26);

        for (int i = 0; i < 4; i++) {
            redPiece.decrementBriefingRounds();
        }

        assertTrue(redPiece.getState().canMove(),
                "After 4 rounds briefing expires, piece can move");
    }

    @Test
    @DisplayName("Capture count increments correctly")
    void captureCountIncrements() {
        assertEquals(0, redPiece.getCaptureCount());
        redPiece.incrementCaptureCount();
        redPiece.incrementCaptureCount();
        assertEquals(2, redPiece.getCaptureCount());
    }

    @Test
    @DisplayName("Piece cannot enter home straight without any capture (Rule T-7)")
    void cannotEnterHomeStraightWithoutCapture() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.incrementApproachPassCount();

        assertFalse(redPiece.canEnterHomeStraight(),
                "Must have at least 1 capture to enter home straight (Rule T-7)");
    }

    @Test
    @DisplayName("Piece can enter home straight after 1 capture and passing approach (clockwise)")
    void canEnterHomeStraightAfterCaptureClockwise() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.incrementCaptureCount();
        redPiece.incrementApproachPassCount();

        assertTrue(redPiece.canEnterHomeStraight(),
                "After 1 capture and passing approach clockwise, piece can enter home straight");
    }

    @Test
    @DisplayName("Counter-clockwise piece requires 2 approach passes to enter home straight (Rule T-1)")
    void counterClockwiseRequiresTwoApproachPasses() {
        redPiece.spawnToStart(Direction.COUNTER_CLOCKWISE);
        redPiece.incrementCaptureCount();
        redPiece.incrementApproachPassCount();

        assertFalse(redPiece.canEnterHomeStraight(),
                "Counter-clockwise piece needs 2 approach passes (Rule T-1)");

        redPiece.incrementApproachPassCount();
        assertTrue(redPiece.canEnterHomeStraight(),
                "After 2 approach passes, counter-clockwise piece can enter home straight");
    }

    @Test
    @DisplayName("Normal movement has no aura modification")
    void normalMovementNoAuraModification() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        assertEquals(4, redPiece.calculateEffectiveMovement(4),
                "No aura: effective movement equals dice roll");
    }

    @Test
    @DisplayName("Restoring original direction after direction change")
    void restoreOriginalDirectionWorks() {
        redPiece.spawnToStart(Direction.CLOCKWISE);
        redPiece.setCurrentDirection(Direction.COUNTER_CLOCKWISE);
        redPiece.restoreOriginalDirection();

        assertEquals(Direction.CLOCKWISE, redPiece.getCurrentDirection(),
                "Original direction should be restored to CLOCKWISE");
    }
}
