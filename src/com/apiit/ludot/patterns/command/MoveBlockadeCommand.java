package com.apiit.ludot.patterns.command;

import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Cell;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.patterns.observer.events.PieceCapturedEvent;
import com.apiit.ludot.patterns.observer.events.PieceMovedEvent;
import com.apiit.ludot.usecases.dto.MoveResultDTO;

import java.util.ArrayList;
import java.util.List;

public class MoveBlockadeCommand implements GameCommand {
    private final Player player;
    private final Blockade blockade;
    private final int diceRoll;
    private final Board board;
    private final BoardCoordinateCalculator calculator;
    private final GameEventPublisher publisher;
    private MoveResultDTO result;

    public MoveBlockadeCommand(Player player, Blockade blockade, int diceRoll, Board board,
                               BoardCoordinateCalculator calculator, GameEventPublisher publisher) {
        this.player = player;
        this.blockade = blockade;
        this.diceRoll = diceRoll;
        this.board = board;
        this.calculator = calculator;
        this.publisher = publisher;
    }

    @Override
    public void execute() {
        int moveDistance = blockade.calculateBlockMoveDistance(diceRoll);
        if (moveDistance <= 0) {
            this.result = MoveResultDTO.failed("Move distance divided by block size yields 0");
            return;
        }

        int currentTrackIndex = blockade.getTrackIndex();
        int distClockwise = calculator.calculateTrackCellDistanceToHome(player.getColor(), currentTrackIndex, Direction.CLOCKWISE);
        int distCounter = calculator.calculateTrackCellDistanceToHome(player.getColor(), currentTrackIndex, Direction.COUNTER_CLOCKWISE);

        Direction blockDirection = blockade.determineBlockDirection(distClockwise, distCounter);
        int delta = blockDirection.getStepDelta();
        int targetTrackIndex = calculator.normalizeTrackIndex(currentTrackIndex + (moveDistance * delta));

        Cell sourceCell = board.getTrackCell(currentTrackIndex);
        Cell targetCell = board.getTrackCell(targetTrackIndex);

        List<Piece> capturedPieces = new ArrayList<>();
        boolean bonusRoll = false;

        if (targetCell.isBlockade() && targetCell.getBlockadeOwner() != player.getColor()) {
            Blockade targetBlockade = board.getBlockadeAt(targetTrackIndex);
            if (targetBlockade != null && blockade.canCapture(targetBlockade)) {

                for (Piece captured : targetBlockade.getPieces()) {
                    targetCell.removeOccupant(captured);
                    captured.resetToBase();
                    capturedPieces.add(captured);
                }

                for (Piece p : blockade.getPieces()) {
                    p.incrementCaptureCount();
                }
                bonusRoll = true;
            }
        }

        List<Piece> piecesToMove = new ArrayList<>(blockade.getPieces());
        for (Piece p : piecesToMove) {
            sourceCell.removeOccupant(p);
            p.moveToTrackCell(targetTrackIndex);
            p.setCurrentDirection(blockDirection);
            targetCell.addOccupant(p);

            publisher.publish(new PieceMovedEvent(
                    player.getColor(),
                    p.getId(),
                    String.valueOf(currentTrackIndex),
                    String.valueOf(targetTrackIndex),
                    moveDistance,
                    blockDirection
            ));
        }

        if (!capturedPieces.isEmpty()) {
            publisher.publish(new PieceCapturedEvent(
                    player.getColor(),
                    blockade.getPieces().get(0).getId() + " Blockade",
                    String.valueOf(targetTrackIndex),
                    capturedPieces.get(0).getColor(),
                    capturedPieces.stream().map(Piece::getId).toList(),
                    player.getPiecesOnBoard().size(),
                    player.getPiecesInBase().size(),
                    true
            ));
        }

        this.result = new MoveResultDTO(
                true,
                bonusRoll,
                !capturedPieces.isEmpty(),
                capturedPieces,
                false,
                String.valueOf(currentTrackIndex),
                String.valueOf(targetTrackIndex),
                "Blockade(" + blockade.size() + ")",
                "Moved blockade to " + targetTrackIndex
        );
    }

    @Override
    public String getDescription() {
        return "Move blockade of " + blockade.size() + " pieces by " + (diceRoll / blockade.size());
    }

    @Override
    public MoveResultDTO getResult() {
        return result;
    }
}
