package com.apiit.ludot.patterns.facade;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.ICoin;
import com.apiit.ludot.domain.service.IDice;
import com.apiit.ludot.domain.service.StandardCoin;
import com.apiit.ludot.domain.service.StandardDice;
import com.apiit.ludot.patterns.gateway.InMemorySimulationGateway;
import com.apiit.ludot.patterns.gateway.SimulationDataGateway;
import com.apiit.ludot.patterns.observer.GameEventListener;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.interactor.GameSimulationInteractor;

import java.util.List;

public class LudoGameFacade {
    private final GameSimulationInteractor interactor;
    private final GameEventPublisher publisher;

    public LudoGameFacade() {
        this.publisher = new GameEventPublisher();
        IDice dice = new StandardDice();
        ICoin coin = new StandardCoin();
        SimulationDataGateway gateway = new InMemorySimulationGateway();
        this.interactor = new GameSimulationInteractor(dice, coin, publisher, gateway);
    }

    public LudoGameFacade(IDice dice, ICoin coin, SimulationDataGateway gateway, GameEventPublisher publisher) {
        this.publisher = publisher;
        this.interactor = new GameSimulationInteractor(dice, coin, publisher, gateway);
    }

    public void addEventListener(GameEventListener listener) {
        publisher.subscribe(listener);
    }

    public void removeEventListener(GameEventListener listener) {
        publisher.unsubscribe(listener);
    }

    public void runGame() {
        interactor.runSimulation();
    }

    public Color getWinnerColor() {
        Player winner = interactor.getWinner();
        return winner != null ? winner.getColor() : null;
    }

    public boolean isGameOver() {
        return interactor.isGameOver();
    }

    public int getRoundsPlayed() {
        return interactor.getRoundNumber();
    }

    public List<Player> getPlayers() {
        return interactor.getPlayers();
    }

    public GameStateDTO getCurrentGameState() {
        return interactor.getCurrentGameState();
    }

    public String getSimulationId() {
        return interactor.getSimulationId();
    }
}
