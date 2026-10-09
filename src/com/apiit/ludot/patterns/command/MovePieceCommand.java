package com.apiit.ludot.patterns.command;

import com.apiit.ludot.domain.model.AuraEffect;
import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Cell;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.PathEvaluationResult;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.patterns.observer.events.MysteryTeleportEvent;
import com.apiit.ludot.patterns.observer.events.PieceBlockedEvent;
import com.apiit.ludot.patterns.observer.events.PieceCapturedEvent;
import com.apiit.ludot.patterns.observer.events.PieceMovedEvent;
import com.apiit.ludot.patterns.singleton.BoardRegistry;
import com.apiit.ludot.usecases.dto.MoveResultDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MovePieceCommand implements GameCommand {
    private final Player player;
    private final Piece piece;
    private final int effectiveUnits;
    private final PathEvaluationResult pathResult;
    private final Board board;
    private final GameEventPublisher publisher;
    private final Random random;
    private MoveResultDTO result;

    public MovePieceCommand(Player player, Piece piece, int effectiveUnits, PathEvaluationResult pathResult,
                            Board board, GameEventPublisher publisher, Random random) {
        this.player = player;
        this.piece = piece;
        this.effectiveUnits = effectiveUnits;
        this.pathResult = pathResult;
        this.board = board;
        this.publisher = publisher;
        this.random = random;
    }

    @Override
    public void execute() {
        String fromLocation = piece.getCurrentCellId();

        if (pathResult.isBlocked()) {
            handleBlockedScenario(fromLocation);
            return;
        }

        removePieceFromCurrentCell();

        if (pathResult.isReachesHome()) {
            piece.enterHome();
            publisher.publish(new PieceMovedEvent(
                    player.getColor(), piece.getId(), fromLocation, "Home",
                    effectiveUnits, piece.getCurrentDirection()
            ));
            this.result = new MoveResultDTO(
                    true, false, false, List.of(), false,
                    fromLocation, "Home", piece.getId(), "Reached Home"
            );
            return;
        }

        if (pathResult.isEntersHomeStraight() || piece.getState().isInHomeStraight()) {
            int straightIndex = pathResult.getTargetHomeStraightIndex();
            piece.enterHomeStraight(straightIndex);
            Cell straightCell = board.getHomeStraightCell(player.getColor(), straightIndex);
            straightCell.addOccupant(piece);

            publisher.publish(new PieceMovedEvent(
                    player.getColor(), piece.getId(), fromLocation, piece.getCurrentCellId(),
                    effectiveUnits, piece.getCurrentDirection()
            ));
            this.result = new MoveResultDTO(
                    true, false, false, List.of(), false,
                    fromLocation, piece.getCurrentCellId(), piece.getId(), "Moved in Home Straight"
            );
            return;
        }

        int targetTrackIndex = pathResult.getTargetTrackIndex();
        Cell targetCell = board.getTrackCell(targetTrackIndex);

        if (pathResult.isPassedApproach()) {
            piece.incrementApproachPassCount();
        }

        List<Piece> captured = new ArrayList<>();
        boolean bonusRoll = false;

        if (!targetCell.isEmpty()) {
            List<Piece> occupants = new ArrayList<>(targetCell.getOccupants());
            for (Piece occupant : occupants) {
                if (occupant.getColor() != player.getColor()) {
                    targetCell.removeOccupant(occupant);
                    occupant.resetToBase();
                    captured.add(occupant);
                    piece.incrementCaptureCount();
                    bonusRoll = true;
                }
            }
        }

        piece.moveToTrackCell(targetTrackIndex);
        targetCell.addOccupant(piece);

        publisher.publish(new PieceMovedEvent(
                player.getColor(), piece.getId(), fromLocation, String.valueOf(targetTrackIndex),
                effectiveUnits, piece.getCurrentDirection()
        ));

        if (!captured.isEmpty()) {
            publisher.publish(new PieceCapturedEvent(
                    player.getColor(),
                    piece.getId(),
                    String.valueOf(targetTrackIndex),
                    captured.get(0).getColor(),
                    captured.stream().map(Piece::getId).toList(),
                    player.getPiecesOnBoard().size(),
                    player.getPiecesInBase().size(),
                    false
            ));
        }

        if (targetCell.isMysteryCell()) {
            resolveMysteryCellLanding(targetCell);
        }

        this.result = new MoveResultDTO(
                true,
                bonusRoll,
                !captured.isEmpty(),
                captured,
                false,
                fromLocation,
                piece.getCurrentCellId(),
                piece.getId(),
                "Moved to " + piece.getCurrentCellId()
        );
    }

    private void handleBlockedScenario(String fromLocation) {
        int cellBeforeIndex = pathResult.getCellBeforeBlockTrackIndex();
        Blockade blockade = pathResult.getBlockingBlockade();
        String blockingName = blockade.getPieces().get(0).getId();

        if (cellBeforeIndex != piece.getTrackIndex()) {

            removePieceFromCurrentCell();
            piece.moveToTrackCell(cellBeforeIndex);
            board.getTrackCell(cellBeforeIndex).addOccupant(piece);

            publisher.publish(new PieceBlockedEvent(
                    player.getColor(), piece.getId(), fromLocation,
                    pathResult.getIntendedTargetCellId(),
                    blockade.getOwnerColor(), blockingName,
                    true, String.valueOf(cellBeforeIndex)
            ));
            this.result = new MoveResultDTO(
                    true, false, false, List.of(), true,
                    fromLocation, String.valueOf(cellBeforeIndex), piece.getId(),
                    "Blocked: moved to adjacent cell before block"
            );
        } else {

            publisher.publish(new PieceBlockedEvent(
                    player.getColor(), piece.getId(), fromLocation,
                    pathResult.getIntendedTargetCellId(),
                    blockade.getOwnerColor(), blockingName,
                    false, String.valueOf(cellBeforeIndex)
            ));
            this.result = new MoveResultDTO(
                    false, false, false, List.of(), true,
                    fromLocation, fromLocation, piece.getId(),
                    "Blocked: cannot move"
            );
        }
    }

    private void resolveMysteryCellLanding(Cell currentCell) {
        int option = random.nextInt(6) + 1;
        currentCell.removeOccupant(piece);

        switch (option) {
            case 1 -> {
                int alphaIndex = BoardRegistry.getInstance().getAlphaCellIndex();
                piece.moveToTrackCell(alphaIndex);
                board.getTrackCell(alphaIndex).addOccupant(piece);

                boolean energized = random.nextBoolean();
                if (energized) {
                    piece.applyAlphaEffect(AuraEffect.ENERGIZED);
                    publisher.publish(new MysteryTeleportEvent(
                            player.getColor(), piece.getId(), "Alpha", String.valueOf(alphaIndex),
                            MysteryTeleportEvent.TeleportEffect.ENERGIZED
                    ));
                } else {
                    piece.applyAlphaEffect(AuraEffect.SICK);
                    publisher.publish(new MysteryTeleportEvent(
                            player.getColor(), piece.getId(), "Alpha", String.valueOf(alphaIndex),
                            MysteryTeleportEvent.TeleportEffect.SICK
                    ));
                }
            }
            case 2 -> {
                int betaIndex = BoardRegistry.getInstance().getBetaCellIndex();
                piece.attendBriefing(betaIndex);
                board.getTrackCell(betaIndex).addOccupant(piece);
                publisher.publish(new MysteryTeleportEvent(
                        player.getColor(), piece.getId(), "Beta", String.valueOf(betaIndex),
                        MysteryTeleportEvent.TeleportEffect.BRIEFING
                ));
            }
            case 3 -> {
                int gammaIndex = BoardRegistry.getInstance().getGammaCellIndex();
                if (piece.getCurrentDirection() == Direction.CLOCKWISE) {
                    piece.setCurrentDirection(Direction.COUNTER_CLOCKWISE);
                    piece.moveToTrackCell(gammaIndex);
                    board.getTrackCell(gammaIndex).addOccupant(piece);
                    publisher.publish(new MysteryTeleportEvent(
                            player.getColor(), piece.getId(), "Gamma", String.valueOf(gammaIndex),
                            MysteryTeleportEvent.TeleportEffect.GAMMA_REVERSE_TO_COUNTER_CLOCKWISE
                    ));
                } else {

                    int betaIndex = BoardRegistry.getInstance().getBetaCellIndex();
                    piece.attendBriefing(betaIndex);
                    board.getTrackCell(betaIndex).addOccupant(piece);
                    publisher.publish(new MysteryTeleportEvent(
                            player.getColor(), piece.getId(), "Beta", String.valueOf(betaIndex),
                            MysteryTeleportEvent.TeleportEffect.GAMMA_TO_BETA
                    ));
                }
            }
            case 4 -> {
                piece.resetToBase();
                publisher.publish(new MysteryTeleportEvent(
                        player.getColor(), piece.getId(), "Base", "Base",
                        MysteryTeleportEvent.TeleportEffect.NONE
                ));
            }
            case 5 -> {
                int startX = player.getColor().getStartXIndex();
                piece.moveToTrackCell(startX);
                board.getTrackCell(startX).addOccupant(piece);
                publisher.publish(new MysteryTeleportEvent(
                        player.getColor(), piece.getId(), "X", String.valueOf(startX),
                        MysteryTeleportEvent.TeleportEffect.NONE
                ));
            }
            case 6 -> {
                int approachIndex = player.getColor().getApproachIndex();
                piece.moveToTrackCell(approachIndex);
                board.getTrackCell(approachIndex).addOccupant(piece);
                publisher.publish(new MysteryTeleportEvent(
                        player.getColor(), piece.getId(), "Approach", String.valueOf(approachIndex),
                        MysteryTeleportEvent.TeleportEffect.NONE
                ));
            }
        }
    }

    private void removePieceFromCurrentCell() {
        if (piece.getState().isOnTrack()) {
            Cell cell = board.getTrackCell(piece.getTrackIndex());
            if (cell != null) {
                cell.removeOccupant(piece);
            }
        } else if (piece.getState().isInHomeStraight()) {
            Cell cell = board.getHomeStraightCell(player.getColor(), piece.getHomeStraightIndex());
            if (cell != null) {
                cell.removeOccupant(piece);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Move piece " + piece.getId() + " by " + effectiveUnits;
    }

    @Override
    public MoveResultDTO getResult() {
        return result;
    }
}
