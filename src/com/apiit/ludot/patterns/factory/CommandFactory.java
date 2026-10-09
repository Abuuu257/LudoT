package com.apiit.ludot.patterns.factory;

import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.domain.service.ICoin;
import com.apiit.ludot.patterns.command.BreakBlockadeCommand;
import com.apiit.ludot.patterns.command.GameCommand;
import com.apiit.ludot.patterns.command.MoveBlockadeCommand;
import com.apiit.ludot.patterns.command.MovePieceCommand;
import com.apiit.ludot.patterns.command.NullCommand;
import com.apiit.ludot.patterns.command.SpawnPieceCommand;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.usecases.dto.MoveCandidate;
import com.apiit.ludot.usecases.dto.MoveDecision;

import java.util.Random;

public class CommandFactory {

    private CommandFactory() {

    }

    public static GameCommand createCommand(Player player, MoveDecision decision, Board board,
                                            ICoin coin, BoardCoordinateCalculator calculator,
                                            GameEventPublisher publisher, Random random) {
        if (!decision.hasMove()) {
            return new NullCommand(decision.getReason());
        }

        MoveCandidate candidate = decision.getCandidate();

        return switch (candidate.moveType()) {
            case SPAWN_FROM_BASE -> new SpawnPieceCommand(
                    player, candidate.piece(), board, coin, publisher
            );
            case MOVE_BLOCKADE -> new MoveBlockadeCommand(
                    player, candidate.participatingBlockade(), candidate.effectiveUnits(),
                    board, calculator, publisher
            );
            case BREAK_BLOCKADE -> new BreakBlockadeCommand(
                    player, candidate.participatingBlockade(), false,
                    candidate.piece(), candidate.effectiveUnits(), board, calculator, publisher, random
            );
            case TRACK_MOVE, BLOCKED_MOVE_TO_ADJACENT, ENTER_HOME_STRAIGHT,
                 HOME_STRAIGHT_ADVANCE, REACH_HOME -> new MovePieceCommand(
                    player, candidate.piece(), candidate.effectiveUnits(),
                    candidate.pathResult(), board, publisher, random
            );
        };
    }
}
