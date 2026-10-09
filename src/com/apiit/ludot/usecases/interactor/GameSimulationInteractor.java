package com.apiit.ludot.usecases.interactor;

import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.BoardCoordinateCalculator;
import com.apiit.ludot.domain.service.ICoin;
import com.apiit.ludot.domain.service.IDice;
import com.apiit.ludot.patterns.factory.StrategyFactory;
import com.apiit.ludot.patterns.gateway.SimulationDataGateway;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.patterns.observer.events.FirstPlayerChosenEvent;
import com.apiit.ludot.patterns.observer.events.GameOverEvent;
import com.apiit.ludot.patterns.observer.events.GameStartedEvent;
import com.apiit.ludot.patterns.observer.events.MysterySpawnedEvent;
import com.apiit.ludot.patterns.observer.events.RoundEndedEvent;
import com.apiit.ludot.usecases.dto.GameStateDTO;
import com.apiit.ludot.usecases.dto.PlayerStatusDTO;
import com.apiit.ludot.usecases.template.StandardTurnExecution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GameSimulationInteractor {
    private static final int MAX_ROUNDS = 1000;

    private final Board board;
    private final List<Player> players;
    private final IDice dice;
    private final GameEventPublisher publisher;
    private final SimulationDataGateway gateway;
    private final Random random;
    private final StandardTurnExecution turnExecution;

    private int roundNumber;
    private boolean gameOver;
    private Player winner;
    private final String simulationId;

    public GameSimulationInteractor(IDice dice, ICoin coin, GameEventPublisher publisher,
                                    SimulationDataGateway gateway) {
        this.dice = dice;
        this.publisher = publisher;
        this.gateway = gateway;
        this.board = new Board();
        this.random = new Random();
        this.players = new ArrayList<>();
        this.simulationId = UUID.randomUUID().toString();

        BoardCoordinateCalculator calculator = new BoardCoordinateCalculator();
        this.turnExecution = new StandardTurnExecution(
                board, calculator, dice, coin, publisher, gateway, random
        );

        initialisePlayersInClockwiseOrder();
    }

    private void initialisePlayersInClockwiseOrder() {

        Color[] clockwiseOrder = {Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE};
        for (Color color : clockwiseOrder) {
            players.add(new Player(color, StrategyFactory.createStrategyForColor(color)));
        }
    }

    public void runSimulation() {
        publisher.publish(new GameStartedEvent(players.stream().map(Player::getColor).toList()));

        determineFirstPlayer();

        List<String> playerOrder = new ArrayList<>();
        for (Player p : players) playerOrder.add(p.getColor().getDisplayName());
        gateway.recordGameStart(simulationId, playerOrder);

        List<Player> finishedPlayers = new ArrayList<>();

        while (!gameOver && roundNumber < MAX_ROUNDS) {
            roundNumber++;
            for (Player currentPlayer : players) {
                if (currentPlayer.isWinner()) {
                    continue;
                }

                GameStateDTO state = buildGameState(currentPlayer);
                turnExecution.executeTurn(currentPlayer, roundNumber, simulationId, state);

                if (currentPlayer.isWinner() && !finishedPlayers.contains(currentPlayer)) {
                    finishedPlayers.add(currentPlayer);
                    int place = finishedPlayers.size();
                    if (winner == null) {
                        winner = currentPlayer;
                    }
                    publisher.publish(new GameOverEvent(
                            currentPlayer.getColor(), roundNumber, place, currentPlayer.getMoveCount()
                    ));

                    if (finishedPlayers.size() >= players.size() - 1) {

                        for (Player p : players) {
                            if (!finishedPlayers.contains(p)) {
                                finishedPlayers.add(p);
                                publisher.publish(new GameOverEvent(
                                        p.getColor(), roundNumber, finishedPlayers.size(), p.getMoveCount()
                                ));
                                break;
                            }
                        }
                        gameOver = true;
                        break;
                    }
                }
            }

            boolean spawned = board.checkAndTriggerMysterySpawn(random);
            if (spawned) {
                publisher.publish(new MysterySpawnedEvent(
                        String.valueOf(board.getMysteryCellLocation()),
                        board.getMysteryRoundsRemaining()
                ));
            }

            publishRoundSummary();

            if (gameOver) break;
        }

        if (!gameOver) {
            gameOver = true;
            if (winner == null) {

                List<Player> remaining = new ArrayList<>(players);
                remaining.sort((p1, p2) -> {
                    int c1 = Integer.compare(p2.getPiecesInHome().size(), p1.getPiecesInHome().size());
                    if (c1 != 0) return c1;
                    return Integer.compare(p1.getMoveCount(), p2.getMoveCount());
                });

                for (Player p : remaining) {
                    if (!finishedPlayers.contains(p)) {
                        finishedPlayers.add(p);
                    }
                }
                winner = finishedPlayers.get(0);
                for (int i = 0; i < finishedPlayers.size(); i++) {
                    Player p = finishedPlayers.get(i);
                    publisher.publish(new GameOverEvent(
                            p.getColor(), roundNumber, i + 1, p.getMoveCount()
                    ));
                }
            }
            publishRoundSummary();
        }

        for (int i = 0; i < finishedPlayers.size(); i++) {
            Player p = finishedPlayers.get(i);
            publisher.publish(new GameOverEvent(
                    p.getColor(), roundNumber, i + 1, p.getMoveCount(), true
            ));
        }

        if (winner != null) {
            gateway.recordGameOver(simulationId, winner.getColor().getDisplayName(), roundNumber);
        }
        gateway.flush();
    }

    private void determineFirstPlayer() {
        Map<Color, Integer> initialRolls = new LinkedHashMap<>();
        int maxRoll = 0;
        Player firstPlayer = players.get(0);

        for (Player player : players) {
            int roll = dice.roll();
            initialRolls.put(player.getColor(), roll);
            if (roll > maxRoll) {
                maxRoll = roll;
                firstPlayer = player;
            }
        }

        int firstIndex = players.indexOf(firstPlayer);

        List<Player> orderedPlayers = new ArrayList<>();
        List<Color> roundOrder = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get((firstIndex + i) % players.size());
            orderedPlayers.add(p);
            roundOrder.add(p.getColor());
        }
        players.clear();
        players.addAll(orderedPlayers);

        publisher.publish(new FirstPlayerChosenEvent(initialRolls, firstPlayer.getColor(), roundOrder));
    }

    private GameStateDTO buildGameState(Player currentPlayer) {
        List<PlayerStatusDTO> statuses = new ArrayList<>();
        for (Player p : players) {
            statuses.add(PlayerStatusDTO.fromEntity(p));
        }
        return new GameStateDTO(
                roundNumber,
                currentPlayer.getColor(),
                0,
                board.getMysteryCellLocation(),
                board.getMysteryRoundsRemaining(),
                statuses,
                gameOver,
                winner != null ? winner.getColor() : null
        );
    }

    private void publishRoundSummary() {
        List<PlayerStatusDTO> statuses = new ArrayList<>();
        for (Player p : players) statuses.add(PlayerStatusDTO.fromEntity(p));

        String mysteryLoc = board.isMysteryCellActive()
                ? String.valueOf(board.getMysteryCellLocation())
                : "none";

        publisher.publish(new RoundEndedEvent(
                roundNumber, statuses,
                board.isMysteryCellActive(), mysteryLoc,
                board.getMysteryRoundsRemaining()
        ));

        if (gateway != null) {
            gateway.recordRoundSummary(simulationId, roundNumber, statuses);
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getWinner() {
        return winner;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Board getBoard() {
        return board;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public GameStateDTO getCurrentGameState() {
        return buildGameState(players.get(0));
    }
}
