package com.apiit.ludot.usecases.template;

import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.domain.service.ICoin;
import com.apiit.ludot.domain.service.IDice;
import com.apiit.ludot.patterns.gateway.SimulationDataGateway;
import com.apiit.ludot.patterns.observer.GameEventPublisher;

import java.util.Random;

public class StandardTurnExecution extends TurnExecutionTemplate {

    public StandardTurnExecution(Board board, BoardCoordinateCalculator calculator, IDice dice,
                                  ICoin coin, GameEventPublisher publisher,
                                  SimulationDataGateway gateway, Random random) {
        super(board, calculator, dice, coin, publisher, gateway, random);
    }
}
