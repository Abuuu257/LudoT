package com.apiit.ludot.patterns.gateway;

import com.apiit.ludot.usecases.dto.PlayerStatusDTO;

import java.util.List;

public interface SimulationDataGateway {
    void recordGameStart(String simulationId, List<String> playerOrder);
    void recordTurn(String simulationId, int roundNumber, String playerColor, int roll, String action);
    void recordCapture(String simulationId, int roundNumber, String capturer, String captured, String cellId);
    void recordMysteryEvent(String simulationId, int roundNumber, String pieceId, String destination, String effect);
    void recordRoundSummary(String simulationId, int roundNumber, List<PlayerStatusDTO> statuses);
    void recordGameOver(String simulationId, String winnerColor, int totalRounds);
    List<String> getTurnLogs(String simulationId);
    void flush();
}
