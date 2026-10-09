package com.apiit.ludot.patterns.command;

import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Cell;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.ICoin;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.patterns.observer.events.PieceCapturedEvent;
import com.apiit.ludot.patterns.observer.events.PieceSpawnedEvent;
import com.apiit.ludot.usecases.dto.MoveResultDTO;

import java.util.ArrayList;
import java.util.List;

public class SpawnPieceCommand implements GameCommand {
    private final Player player;
    private final Piece piece;
    private final Board board;
    private final ICoin coin;
    private final GameEventPublisher publisher;
    private MoveResultDTO result;

    public SpawnPieceCommand(Player player, Piece piece, Board board, ICoin coin, GameEventPublisher publisher) {
        this.player = player;
        this.piece = piece;
        this.board = board;
        this.coin = coin;
        this.publisher = publisher;
    }

    @Override
    public void execute() {

        Direction direction = coin.toss();
        piece.spawnToStart(direction);

        int startX = player.getColor().getStartXIndex();
        Cell startCell = board.getTrackCell(startX);

        List<Piece> captured = new ArrayList<>();
        if (!startCell.isEmpty()) {
            List<Piece> currentOccupants = new ArrayList<>(startCell.getOccupants());
            for (Piece occupant : currentOccupants) {
                if (occupant.getColor() != player.getColor()) {
                    startCell.removeOccupant(occupant);
                    occupant.resetToBase();
                    captured.add(occupant);
                    piece.incrementCaptureCount();
                }
            }
        }

        startCell.addOccupant(piece);

        String coinResult = (direction == Direction.CLOCKWISE) ? "Heads" : "Tails";
        publisher.publish(new PieceSpawnedEvent(
                player.getColor(),
                piece.getId(),
                direction,
                coinResult,
                player.getPiecesOnBoard().size(),
                player.getPiecesInBase().size()
        ));

        if (!captured.isEmpty()) {
            publisher.publish(new PieceCapturedEvent(
                    player.getColor(),
                    piece.getId(),
                    String.valueOf(startX),
                    captured.get(0).getColor(),
                    captured.stream().map(Piece::getId).toList(),
                    player.getPiecesOnBoard().size(),
                    player.getPiecesInBase().size(),
                    false
            ));
        }

        this.result = new MoveResultDTO(
                true,
                true,
                !captured.isEmpty(),
                captured,
                false,
                "Base",
                String.valueOf(startX),
                piece.getId(),
                "Spawned piece " + piece.getId() + " to " + startX
        );
    }

    @Override
    public String getDescription() {
        return "Spawn " + piece.getId() + " from Base to X";
    }

    @Override
    public MoveResultDTO getResult() {
        return result;
    }
}
