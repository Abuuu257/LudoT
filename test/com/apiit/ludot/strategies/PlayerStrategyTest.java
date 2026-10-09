package com.apiit.ludot.strategies;

import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.patterns.strategy.BlueMysteryStrategy;
import com.apiit.ludot.patterns.strategy.GreenBlockerStrategy;
import com.apiit.ludot.patterns.strategy.NullPlayerStrategy;
import com.apiit.ludot.patterns.strategy.RedAggressiveStrategy;
import com.apiit.ludot.patterns.strategy.YellowRacerStrategy;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.dto.MoveCandidate;
import com.apiit.ludot.usecases.dto.MoveDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Player Strategy Tests")
class PlayerStrategyTest {

    private BoardCoordinateCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new BoardCoordinateCalculator();
    }

    private GameStateDTO emptyGameState(Color current) {
        return new GameStateDTO(1, current, 6, -1, 0, List.of(), false, null);
    }

    private MoveCandidate buildTrackMove(Piece piece, int effectiveUnits, boolean isCapturing,
                                         Piece opponent, boolean creatingBlock, int distToHome) {
        return new MoveCandidate(
                piece, MoveCandidate.CandidateMoveType.TRACK_MOVE, effectiveUnits,
                null, isCapturing, opponent, false, null,
                creatingBlock, false, null, false, false, distToHome
        );
    }

    private MoveCandidate buildSpawnMove(Piece piece) {
        return new MoveCandidate(
                piece, MoveCandidate.CandidateMoveType.SPAWN_FROM_BASE, 6,
                null, false, null, false, null,
                false, false, null, false, false, 57
        );
    }

    @Test
    @DisplayName("[Red] Prioritises capturing opponent over other moves")
    void redPrioritisesCapture() {
        Player redPlayer = new Player(Color.RED, new RedAggressiveStrategy());
        Piece p1 = redPlayer.getPiece(1);
        p1.spawnToStart(Direction.CLOCKWISE);
        p1.moveToTrackCell(5);

        Piece opponent = new Piece(Color.GREEN, 1);
        opponent.spawnToStart(Direction.CLOCKWISE);
        opponent.moveToTrackCell(11);

        MoveCandidate normalMove = buildTrackMove(p1, 3, false, null, false, 40);
        MoveCandidate capturingMove = buildTrackMove(p1, 6, true, opponent, false, 35);

        List<MoveCandidate> candidates = List.of(normalMove, capturingMove);
        MoveDecision decision = redPlayer.getStrategy().decideMove(
                redPlayer, candidates, emptyGameState(Color.RED), calculator);

        assertTrue(decision.hasMove());
        assertEquals(MoveCandidate.CandidateMoveType.TRACK_MOVE, decision.getCandidate().moveType());
        assertTrue(decision.getCandidate().isCapturingOpponent(),
                "Red should choose the capturing move");
    }

    @Test
    @DisplayName("[Red] Spawns from base on six when no capture is possible")
    void redSpawnsFromBaseWhenNoCaptureAvailable() {
        Player redPlayer = new Player(Color.RED, new RedAggressiveStrategy());
        Piece p = redPlayer.getPiece(1);

        MoveCandidate spawnMove = buildSpawnMove(p);
        Piece p2 = new Piece(Color.RED, 2);
        p2.spawnToStart(Direction.CLOCKWISE);
        MoveCandidate normalMove = buildTrackMove(p2, 4, false, null, false, 40);

        List<MoveCandidate> candidates = List.of(normalMove, spawnMove);
        MoveDecision decision = redPlayer.getStrategy().decideMove(
                redPlayer, candidates, emptyGameState(Color.RED), calculator);

        assertTrue(decision.hasMove());
        assertEquals(MoveCandidate.CandidateMoveType.SPAWN_FROM_BASE, decision.getCandidate().moveType(),
                "Red should spawn from base when no capture available on 6");
    }

    @Test
    @DisplayName("[Green] Prioritises creating a blockade on 6-roll")
    void greenPrioritisesCreatingBlockade() {
        Player greenPlayer = new Player(Color.GREEN, new GreenBlockerStrategy());
        Piece p1 = greenPlayer.getPiece(1);
        Piece p2 = greenPlayer.getPiece(2);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);

        MoveCandidate normalMove = buildTrackMove(p1, 6, false, null, false, 40);
        MoveCandidate blockCreatingMove = buildTrackMove(p2, 6, false, null, true, 38);

        List<MoveCandidate> candidates = List.of(normalMove, blockCreatingMove);
        MoveDecision decision = greenPlayer.getStrategy().decideMove(
                greenPlayer, candidates, emptyGameState(Color.GREEN), calculator);

        assertTrue(decision.hasMove());
        assertTrue(decision.getCandidate().isCreatingBlockade(),
                "Green should choose the block-creating move");
    }

    @Test
    @DisplayName("[Green] Avoids breaking blockade when other moves available")
    void greenAvoidsBreakingBlockade() {
        Player greenPlayer = new Player(Color.GREEN, new GreenBlockerStrategy());
        Piece p1 = greenPlayer.getPiece(1);
        Piece p2 = greenPlayer.getPiece(2);
        p1.spawnToStart(Direction.CLOCKWISE);
        p2.spawnToStart(Direction.CLOCKWISE);

        Piece blockadePiece = greenPlayer.getPiece(3);
        blockadePiece.spawnToStart(Direction.CLOCKWISE);

        MoveCandidate normalMove = buildTrackMove(p1, 4, false, null, false, 35);

        Blockade blockade = new Blockade(Color.GREEN, 39, List.of(p2, blockadePiece));
        MoveCandidate breakMove = new MoveCandidate(
                p2, MoveCandidate.CandidateMoveType.BREAK_BLOCKADE, 4,
                null, false, null, false, null,
                false, false, blockade, true, false, 33
        );

        List<MoveCandidate> candidates = List.of(normalMove, breakMove);
        MoveDecision decision = greenPlayer.getStrategy().decideMove(
                greenPlayer, candidates, emptyGameState(Color.GREEN), calculator);

        assertTrue(decision.hasMove());
        assertFalse(decision.getCandidate().isBreakingBlockade(),
                "Green should prefer other moves over breaking its blockade");
    }

    @Test
    @DisplayName("[Yellow] Spawns from base on 6-roll (empty base preference)")
    void yellowSpawnsFromBaseOnSix() {
        Player yellowPlayer = new Player(Color.YELLOW, new YellowRacerStrategy());
        Piece basePiece = yellowPlayer.getPiece(1);

        Piece trackPiece = yellowPlayer.getPiece(2);
        trackPiece.spawnToStart(Direction.CLOCKWISE);

        MoveCandidate spawnMove = buildSpawnMove(basePiece);
        MoveCandidate trackMove = buildTrackMove(trackPiece, 6, false, null, false, 30);

        List<MoveCandidate> candidates = List.of(trackMove, spawnMove);
        MoveDecision decision = yellowPlayer.getStrategy().decideMove(
                yellowPlayer, candidates, emptyGameState(Color.YELLOW), calculator);

        assertTrue(decision.hasMove());
        assertEquals(MoveCandidate.CandidateMoveType.SPAWN_FROM_BASE, decision.getCandidate().moveType(),
                "Yellow always spawns from base on 6 (empty base preference)");
    }

    @Test
    @DisplayName("[Yellow] Moves piece closest to home when no spawn available")
    void yellowMovesPieceClosestToHome() {
        Player yellowPlayer = new Player(Color.YELLOW, new YellowRacerStrategy());
        Piece farPiece = yellowPlayer.getPiece(1);
        Piece nearPiece = yellowPlayer.getPiece(2);
        farPiece.spawnToStart(Direction.CLOCKWISE);
        farPiece.moveToTrackCell(10);
        nearPiece.spawnToStart(Direction.CLOCKWISE);
        nearPiece.moveToTrackCell(45);

        MoveCandidate farMove = buildTrackMove(farPiece, 3, false, null, false, 40);
        MoveCandidate nearMove = buildTrackMove(nearPiece, 3, false, null, false, 10);

        List<MoveCandidate> candidates = List.of(farMove, nearMove);
        MoveDecision decision = yellowPlayer.getStrategy().decideMove(
                yellowPlayer, candidates, emptyGameState(Color.YELLOW), calculator);

        assertTrue(decision.hasMove());
        assertEquals(nearPiece, decision.getCandidate().piece(),
                "Yellow should move the piece closest to home");
    }

    @Test
    @DisplayName("[Blue] Cycles piece selection in order B1->B2->B3->B4")
    void blueCyclesPiecesInOrder() {
        BlueMysteryStrategy blueStrategy = new BlueMysteryStrategy();
        blueStrategy.setNextCyclicPieceNumber(1);
        Player bluePlayer = new Player(Color.BLUE, blueStrategy);

        Piece b1 = bluePlayer.getPiece(1);
        Piece b2 = bluePlayer.getPiece(2);
        b1.spawnToStart(Direction.CLOCKWISE);
        b2.spawnToStart(Direction.CLOCKWISE);

        MoveCandidate moveB1 = buildTrackMove(b1, 3, false, null, false, 30);
        MoveCandidate moveB2 = buildTrackMove(b2, 3, false, null, false, 35);

        List<MoveCandidate> candidates = List.of(moveB2, moveB1);
        MoveDecision decision = bluePlayer.getStrategy().decideMove(
                bluePlayer, candidates, emptyGameState(Color.BLUE), calculator);

        assertTrue(decision.hasMove());
        assertEquals(b1, decision.getCandidate().piece(),
                "Blue should pick piece B1 first in cyclic order");
    }

    @Test
    @DisplayName("[Blue] Counter-clockwise piece prefers mystery cell landing")
    void blueCounterClockwisePrefersMysteryCellLanding() {
        BlueMysteryStrategy blueStrategy = new BlueMysteryStrategy();
        Player bluePlayer = new Player(Color.BLUE, blueStrategy);

        Piece b1 = bluePlayer.getPiece(1);
        b1.spawnToStart(Direction.COUNTER_CLOCKWISE);

        MoveCandidate normalMove = buildTrackMove(b1, 4, false, null, false, 30);
        MoveCandidate mysteryMove = new MoveCandidate(
                b1, MoveCandidate.CandidateMoveType.TRACK_MOVE, 4,
                null, false, null, false, null,
                false, false, null, false, true, 28
        );

        List<MoveCandidate> candidates = List.of(normalMove, mysteryMove);
        MoveDecision decision = bluePlayer.getStrategy().decideMove(
                bluePlayer, candidates, emptyGameState(Color.BLUE), calculator);

        assertTrue(decision.hasMove());
        assertTrue(decision.getCandidate().isLandingOnMystery(),
                "Counter-clockwise Blue piece should prefer landing on mystery cell");
    }

    @Test
    @DisplayName("Null strategy returns first available move")
    void nullStrategyReturnsMoveWhenAvailable() {
        Player player = new Player(Color.RED, new NullPlayerStrategy());
        Piece p = player.getPiece(1);
        p.spawnToStart(Direction.CLOCKWISE);

        MoveCandidate move = buildTrackMove(p, 3, false, null, false, 40);
        MoveDecision decision = player.getStrategy().decideMove(
                player, List.of(move), emptyGameState(Color.RED), calculator);

        assertTrue(decision.hasMove());
    }

    @Test
    @DisplayName("Null strategy returns NullMove when no candidates")
    void nullStrategyReturnsNullMoveWhenNoCandidates() {
        Player player = new Player(Color.RED, new NullPlayerStrategy());
        MoveDecision decision = player.getStrategy().decideMove(
                player, Collections.emptyList(), emptyGameState(Color.RED), calculator);

        assertFalse(decision.hasMove(), "No candidates should yield NullMove");
    }
}
