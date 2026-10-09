package com.apiit.ludot.patterns.command;

import com.apiit.ludot.domain.model.Blockade;
import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.domain.service.PathEvaluationResult;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.usecases.dto.MoveResultDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BreakBlockadeCommand implements GameCommand {
    private final Player player;
    private final Blockade blockade;
    private final boolean triggeredByThreeConsecutiveSixes;
    private final Piece specificPieceToMove;
    private final int moveUnits;
    private final Board board;
    private final BoardCoordinateCalculator calculator;
    private final GameEventPublisher publisher;
    private final Random random;
    private MoveResultDTO result;

    public BreakBlockadeCommand(Player player, Blockade blockade, boolean triggeredByThreeConsecutiveSixes,
                                Piece specificPieceToMove, int moveUnits, Board board,
                                BoardCoordinateCalculator calculator, GameEventPublisher publisher, Random random) {
        this.player = player;
        this.blockade = blockade;
        this.triggeredByThreeConsecutiveSixes = triggeredByThreeConsecutiveSixes;
        this.specificPieceToMove = specificPieceToMove;
        this.moveUnits = moveUnits;
        this.board = board;
        this.calculator = calculator;
        this.publisher = publisher;
        this.random = random;
    }

    @Override
    public void execute() {
        if (triggeredByThreeConsecutiveSixes) {

            List<Piece> pieces = new ArrayList<>(blockade.getPieces());

            int remainingPieceCount = pieces.size() - 1;
            int unitsPerPiece = Math.max(1, 6 / remainingPieceCount);

            for (int i = 1; i < pieces.size(); i++) {
                Piece p = pieces.get(i);
                p.restoreOriginalDirection();

                PathEvaluationResult path = calculator.evaluateMove(p, unitsPerPiece, board);
                MovePieceCommand subCmd = new MovePieceCommand(
                        player, p, unitsPerPiece, path, board, publisher, random
                );
                subCmd.execute();
            }

            this.result = new MoveResultDTO(
                    true, false, false, List.of(), false,
                    String.valueOf(blockade.getTrackIndex()), "Multiple",
                    "Blockade", "Broken by 3 consecutive sixes (Rule T-6)"
            );
        } else {

            specificPieceToMove.restoreOriginalDirection();
            PathEvaluationResult path = calculator.evaluateMove(specificPieceToMove, moveUnits, board);
            MovePieceCommand subCmd = new MovePieceCommand(
                    player, specificPieceToMove, moveUnits, path, board, publisher, random
            );
            subCmd.execute();
            this.result = subCmd.getResult();
        }
    }

    @Override
    public String getDescription() {
        return "Break blockade of " + blockade.size() + " pieces";
    }

    @Override
    public MoveResultDTO getResult() {
        return result;
    }
}
