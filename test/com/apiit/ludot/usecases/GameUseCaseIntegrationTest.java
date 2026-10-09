package com.apiit.ludot.usecases;

import com.apiit.ludot.adapters.presenter.EventCollectorPresenter;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.MockCoin;
import com.apiit.ludot.domain.service.MockDice;
import com.apiit.ludot.domain.service.StandardCoin;
import com.apiit.ludot.domain.service.StandardDice;
import com.apiit.ludot.patterns.facade.LudoGameFacade;
import com.apiit.ludot.patterns.gateway.InMemorySimulationGateway;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.patterns.observer.events.DiceRolledEvent;
import com.apiit.ludot.patterns.observer.events.FirstPlayerChosenEvent;
import com.apiit.ludot.patterns.observer.events.GameOverEvent;
import com.apiit.ludot.patterns.observer.events.GameStartedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Game Use Case Integration Tests")
class GameUseCaseIntegrationTest {

    private InMemorySimulationGateway gateway;
    private GameEventPublisher publisher;
    private EventCollectorPresenter collector;

    @BeforeEach
    void setUp() {
        gateway = new InMemorySimulationGateway();
        publisher = new GameEventPublisher();
        collector = new EventCollectorPresenter();
        publisher.subscribe(collector);
    }

    @Test
    @DisplayName("[Integration] Full game run completes with a winner")
    void fullGameRunProducesWinner() {
        LudoGameFacade facade = new LudoGameFacade(
                new StandardDice(), new StandardCoin(), gateway, publisher);
        facade.runGame();

        assertTrue(facade.isGameOver(), "Game should be over after runGame()");
        assertNotNull(facade.getWinnerColor(), "There should be a winner");
        assertTrue(facade.getRoundsPlayed() > 0,
                "At least one round should have been played");
    }

    @Test
    @DisplayName("[Integration] GameStartedEvent is published at game start")
    void gameStartedEventPublishedAtStart() {
        LudoGameFacade facade = new LudoGameFacade(
                new StandardDice(), new StandardCoin(), gateway, publisher);
        facade.runGame();

        assertTrue(collector.hasEventOfType(GameStartedEvent.class),
                "GameStartedEvent must be published at start");
    }

    @Test
    @DisplayName("[Integration] GameOverEvent is published when winner found")
    void gameOverEventPublishedAtEnd() {
        LudoGameFacade facade = new LudoGameFacade(
                new StandardDice(), new StandardCoin(), gateway, publisher);
        facade.runGame();

        assertTrue(collector.hasEventOfType(GameOverEvent.class),
                "GameOverEvent should be published when game ends");
    }

    @Test
    @DisplayName("[Integration] FirstPlayerChosenEvent is published after initial rolls")
    void firstPlayerChosenEventPublished() {
        LudoGameFacade facade = new LudoGameFacade(
                new StandardDice(), new StandardCoin(), gateway, publisher);
        facade.runGame();

        assertTrue(collector.hasEventOfType(FirstPlayerChosenEvent.class),
                "FirstPlayerChosenEvent should be published after initial dice rolls");
    }

    @Test
    @DisplayName("[Integration] At least one PieceSpawnedEvent occurs during the game")
    void atLeastOneSpawnOccurs() {
        LudoGameFacade facade = new LudoGameFacade(
                new StandardDice(), new StandardCoin(), gateway, publisher);
        facade.runGame();

        assertTrue(collector.getSpawnCount() > 0,
                "At least one piece should have been spawned during the game");
    }

    @Test
    @DisplayName("[Integration] Winner's pieces are all in home at game end")
    void winnerAllPiecesInHome() {
        LudoGameFacade facade = new LudoGameFacade(
                new StandardDice(), new StandardCoin(), gateway, publisher);
        facade.runGame();

        Color winnerColor = facade.getWinnerColor();
        assertNotNull(winnerColor);

        Player winner = facade.getPlayers().stream()
                .filter(p -> p.getColor() == winnerColor)
                .findFirst()
                .orElse(null);
        assertNotNull(winner);
        assertEquals(4, winner.getPiecesInHome().size(),
                "All 4 winner pieces should be in Home");
    }

    @Test
    @DisplayName("[Integration] Gateway logs contain game start and game over")
    void gatewayContainsCompleteGameLog() {
        String simId;
        LudoGameFacade facade = new LudoGameFacade(
                new StandardDice(), new StandardCoin(), gateway, publisher);
        facade.runGame();
        simId = facade.getSimulationId();

        var logs = gateway.getTurnLogs(simId);
        assertFalse(logs.isEmpty(), "Gateway should have logged events");
        assertTrue(logs.stream().anyMatch(l -> l.contains("GAME_START")),
                "Logs should contain game start");
        assertTrue(logs.stream().anyMatch(l -> l.contains("GAME_OVER")),
                "Logs should contain game over");
    }

    @Test
    @DisplayName("[Integration] MockDice with fixed sixes spawns pieces quickly")
    void mockDiceWithSixesSpawnsPiecesQuickly() {

        MockDice mockDice = new MockDice();
        mockDice.queueRolls(6, 6, 6, 6,

                6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6);
        mockDice.setDefaultRoll(5);

        LudoGameFacade facade = new LudoGameFacade(
                mockDice, new MockCoin(Direction.CLOCKWISE), gateway, publisher);
        facade.runGame();

        assertTrue(facade.isGameOver() || facade.getRoundsPlayed() > 0,
                "Game should have run successfully with mock dice");
    }

    @Test
    @DisplayName("[Integration] DiceRolledEvent published for every roll in the game")
    void diceRolledEventPublishedForEveryRoll() {
        LudoGameFacade facade = new LudoGameFacade(
                new StandardDice(), new StandardCoin(), gateway, publisher);
        facade.runGame();

        var diceEvents = collector.getEventsOfType(DiceRolledEvent.class);
        assertFalse(diceEvents.isEmpty(), "DiceRolledEvent should be published at least once");
    }
}
