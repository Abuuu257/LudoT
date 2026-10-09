package com.apiit.ludot.usecases.template;

import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Cell;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.domain.service.ICoin;
import com.apiit.ludot.domain.service.IDice;
import com.apiit.ludot.domain.service.PathEvaluationResult;
import com.apiit.ludot.patterns.command.BreakBlockadeCommand;
import com.apiit.ludot.patterns.command.GameCommand;
import com.apiit.ludot.patterns.factory.CommandFactory;
import com.apiit.ludot.patterns.gateway.SimulationDataGateway;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.patterns.observer.events.DiceRolledEvent;
import com.apiit.ludot.patterns.observer.events.MysteryTeleportEvent;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.dto.MoveCandidate;
import com.apiit.ludot.usecases.dto.MoveDecision;
import com.apiit.ludot.usecases.dto.MoveResultDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class TurnExecutionTemplate {
    protected final Board board;
    protected final BoardCoordinateCalculator calculator;
    protected final IDice dice;
    protected final ICoin coin;
    protected final GameEventPublisher publisher;
    protected final SimulationDataGateway gateway;
    protected final Random random;

    public TurnExecutionTemplate(Board board, BoardCoordinateCalculator calculator, IDice dice,
                                 ICoin coin, GameEventPublisher publisher, SimulationDataGateway gateway,
                                 Random random) {
        this.board = board;
        this.calculator = calculator;
        this.dice = dice;
        this.coin = coin;
        this.publisher = publisher;
        this.gateway = gateway;
        this.random = random;
    }

    public final void executeTurn(Player player, int roundNumber, String simulationId, GameStateDTO state) {
        boolean allowAnotherRoll = true;

        while (allowAnotherRoll) {
            int roll = dice.roll();
            publisher.publish(new DiceRolledEvent(player.getColor(), roll));

            checkBriefingRollThreeRule(player, roll);

            if (roll == 6) {
                player.incrementConsecutiveSixes();
                if (player.getConsecutiveSixes() == 3) {
                    handleThreeConsecutiveSixes(player, roundNumber, simulationId);
                    player.resetConsecutiveSixes();
                    break;
                }
            } else {
                player.resetConsecutiveSixes();
            }

            List<MoveCandidate> candidates = generateCandidateMoves(player, roll);

            MoveDecision decision = player.getStrategy().decideMove(player, candidates, state, calculator);

            GameCommand command = CommandFactory.createCommand(
                    player, decision, board, coin, calculator, publisher, random
            );
            command.execute();
            MoveResultDTO result = command.getResult();
            if (result != null && result.success()) {
                player.incrementMoveCount();
            }

            if (gateway != null) {
                gateway.recordTurn(simulationId, roundNumber, player.getColor().getDisplayName(),
                        roll, command.getDescription());
            }

            allowAnotherRoll = shouldGrantBonusRoll(player, result, roll);
        }
    }

    private void checkBriefingRollThreeRule(Player player, int roll) {
        if (roll == 3) {
            player.incrementConsecutiveThrees();
            if (player.getConsecutiveThrees() >= 2 && player.hasPieceInBriefing()) {

                for (Piece p : player.getPieces()) {
                    if (p.getState().isRestricted()) {
                        board.getTrackCell(p.getTrackIndex()).removeOccupant(p);
                        p.resetToBase();
                        publisher.publish(new MysteryTeleportEvent(
                                player.getColor(), p.getId(), "Base", "Base",
                                MysteryTeleportEvent.TeleportEffect.BRIEFING_ROLL_THREE_TELEPORT_BASE
                        ));
                    }
                }
                player.resetConsecutiveThrees();
            }
        } else {
            player.resetConsecutiveThrees();
        }
    }

    private void handleThreeConsecutiveSixes(Player player, int roundNumber, String simulationId) {
        if (player.hasBlockade(board)) {

            List<Blockade> blockades = player.getBlockades(board);
            BreakBlockadeCommand breakCmd = new BreakBlockadeCommand(
                    player, blockades.get(0), true, null, 6, board, calculator, publisher, random
            );
            breakCmd.execute();
            if (gateway != null) {
                gateway.recordTurn(simulationId, roundNumber, player.getColor().getDisplayName(), 6,
                        "Broke blockade due to 3 consecutive sixes (Rule T-6)");
            }
        }
    }

    public List<MoveCandidate> generateCandidateMoves(Player player, int roll) {
        List<MoveCandidate> candidates = new ArrayList<>();

        if (roll == 6) {
            for (Piece piece : player.getPiecesInBase()) {
                candidates.add(new MoveCandidate(
                        piece,
                        MoveCandidate.CandidateMoveType.SPAWN_FROM_BASE,
                        roll,
                        null,
                        false, null, false, null,
                        false, false, null, false, false,
                        calculator.calculateDistanceToHome(piece)
                ));
            }
        }

        for (Piece piece : player.getPieces()) {
            if (piece.getState().canMove() || piece.getState().isInHomeStraight()) {
                int effectiveUnits = piece.calculateEffectiveMovement(roll);
                PathEvaluationResult path = calculator.evaluateMove(piece, effectiveUnits, board);

                if (path.isValid()) {
                    boolean isCapturingOpponent = false;
                    Piece opponentToCapture = null;
                    boolean isCapturingBlockade = false;
                    Blockade blockadeToCapture = null;
                    boolean isCreatingBlockade = false;
                    boolean isLandingOnMystery = false;

                    if (!path.isReachesHome() && !path.isEntersHomeStraight() && !piece.getState().isInHomeStraight()) {
                        Cell targetCell = board.getTrackCell(path.getTargetTrackIndex());
                        if (targetCell.isMysteryCell()) {
                            isLandingOnMystery = true;
                        }
                        if (!targetCell.isEmpty()) {
                            if (targetCell.getBlockadeOwner() == player.getColor()) {
                                isCreatingBlockade = true;
                            } else {

                                for (Piece occ : targetCell.getOccupants()) {
                                    if (occ.getColor() != player.getColor()) {
                                        isCapturingOpponent = true;
                                        opponentToCapture = occ;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    MoveCandidate.CandidateMoveType moveType;
                    if (path.isReachesHome()) {
                        moveType = MoveCandidate.CandidateMoveType.REACH_HOME;
                    } else if (path.isEntersHomeStraight()) {
                        moveType = MoveCandidate.CandidateMoveType.ENTER_HOME_STRAIGHT;
                    } else if (piece.getState().isInHomeStraight()) {
                        moveType = MoveCandidate.CandidateMoveType.HOME_STRAIGHT_ADVANCE;
                    } else {
                        moveType = MoveCandidate.CandidateMoveType.TRACK_MOVE;
                    }

                    candidates.add(new MoveCandidate(
                            piece, moveType, effectiveUnits, path,
                            isCapturingOpponent, opponentToCapture,
                            isCapturingBlockade, blockadeToCapture,
                            isCreatingBlockade, false, null, false,
                            isLandingOnMystery,
                            path.isReachesHome() ? 0 : calculator.calculateDistanceToHome(piece) - effectiveUnits
                    ));
                } else if (path.isBlocked() && path.getCellBeforeBlockTrackIndex() != piece.getTrackIndex()) {

                    candidates.add(new MoveCandidate(
                            piece, MoveCandidate.CandidateMoveType.BLOCKED_MOVE_TO_ADJACENT,
                            effectiveUnits, path,
                            false, null, false, null,
                            false, false, null, false, false,
                            calculator.calculateDistanceToHome(piece)
                    ));
                }
            }
        }

        List<Blockade> blockades = player.getBlockades(board);
        for (Blockade b : blockades) {
            int blockDist = b.calculateBlockMoveDistance(roll);
            if (blockDist > 0) {

                candidates.add(new MoveCandidate(
                        b.getPieces().get(0),
                        MoveCandidate.CandidateMoveType.MOVE_BLOCKADE,
                        blockDist,
                        null,
                        false, null, false, null,
                        false, true, b, false, false,
                        calculator.calculateDistanceToHome(b.getPieces().get(0))
                ));
            }

            for (Piece piece : b.getPieces()) {
                PathEvaluationResult path = calculator.evaluateMove(piece, roll, board);
                if (path.isValid()) {
                    candidates.add(new MoveCandidate(
                            piece,
                            MoveCandidate.CandidateMoveType.BREAK_BLOCKADE,
                            roll,
                            path,
                            false, null, false, null,
                            false, false, b, true, false,
                            calculator.calculateDistanceToHome(piece) - roll
                    ));
                }
            }
        }

        return candidates;
    }

    protected boolean shouldGrantBonusRoll(Player player, MoveResultDTO result, int roll) {
        if (player.isWinner()) {
            return false;
        }

        if (roll == 6 && player.getConsecutiveSixes() < 3) {
            return true;
        }

        return result != null && result.bonusRollEarned();
    }
}
