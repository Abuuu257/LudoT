package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.patterns.observer.GameEvent;
import com.apiit.ludot.usecases.dto.PlayerStatusDTO;

import java.util.List;

public record RoundEndedEvent(
        int roundNumber,
        List<PlayerStatusDTO> playerStatuses,
        boolean mysteryCellActive,
        String mysteryCellLocation,
        int mysteryRoundsRemaining,
        long timestamp
) implements GameEvent {
    public RoundEndedEvent(int roundNumber, List<PlayerStatusDTO> playerStatuses,
                           boolean mysteryCellActive, String mysteryCellLocation, int mysteryRoundsRemaining) {
        this(roundNumber, playerStatuses, mysteryCellActive, mysteryCellLocation, mysteryRoundsRemaining, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
