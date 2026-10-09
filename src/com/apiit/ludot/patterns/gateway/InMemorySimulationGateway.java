package com.apiit.ludot.patterns.gateway;

import com.apiit.ludot.usecases.dto.PlayerStatusDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemorySimulationGateway implements SimulationDataGateway {
    private final Map<String, List<String>> simulationLogs = new HashMap<>();
    private final Map<String, List<String>> captureLogs = new HashMap<>();
    private final Map<String, List<String>> mysteryLogs = new HashMap<>();
    private final Map<String, String> gameWinners = new HashMap<>();

    @Override
    public void recordGameStart(String simulationId, List<String> playerOrder) {
        simulationLogs.computeIfAbsent(simulationId, k -> new ArrayList<>())
                .add("GAME_START: Order=" + playerOrder);
    }

    @Override
    public void recordTurn(String simulationId, int roundNumber, String playerColor, int roll, String action) {
        simulationLogs.computeIfAbsent(simulationId, k -> new ArrayList<>())
                .add("TURN: Round " + roundNumber + " | " + playerColor + " rolled " + roll + " -> " + action);
    }

    @Override
    public void recordCapture(String simulationId, int roundNumber, String capturer, String captured, String cellId) {
        captureLogs.computeIfAbsent(simulationId, k -> new ArrayList<>())
                .add("CAPTURE: Round " + roundNumber + " | " + capturer + " captured " + captured + " at cell " + cellId);
    }

    @Override
    public void recordMysteryEvent(String simulationId, int roundNumber, String pieceId, String destination, String effect) {
        mysteryLogs.computeIfAbsent(simulationId, k -> new ArrayList<>())
                .add("MYSTERY: Round " + roundNumber + " | " + pieceId + " teleported to " + destination + " [" + effect + "]");
    }

    @Override
    public void recordRoundSummary(String simulationId, int roundNumber, List<PlayerStatusDTO> statuses) {
        simulationLogs.computeIfAbsent(simulationId, k -> new ArrayList<>())
                .add("ROUND_SUMMARY: Round " + roundNumber + " complete");
    }

    @Override
    public void recordGameOver(String simulationId, String winnerColor, int totalRounds) {
        gameWinners.put(simulationId, winnerColor);
        simulationLogs.computeIfAbsent(simulationId, k -> new ArrayList<>())
                .add("GAME_OVER: Winner=" + winnerColor + " after " + totalRounds + " rounds");
    }

    @Override
    public List<String> getTurnLogs(String simulationId) {
        return Collections.unmodifiableList(simulationLogs.getOrDefault(simulationId, List.of()));
    }

    public List<String> getCaptureLogs(String simulationId) {
        return Collections.unmodifiableList(captureLogs.getOrDefault(simulationId, List.of()));
    }

    public List<String> getMysteryLogs(String simulationId) {
        return Collections.unmodifiableList(mysteryLogs.getOrDefault(simulationId, List.of()));
    }

    @Override
    public void flush() {

    }
}
