package com.apiit.ludot.patterns.observer.events;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.patterns.observer.GameEvent;

public record MysteryTeleportEvent(
        Color playerColor,
        String pieceName,
        String targetDestinationName,
        String targetCellId,
        TeleportEffect effect,
        long timestamp
) implements GameEvent {

    public enum TeleportEffect {
        ENERGIZED,
        SICK,
        BRIEFING,
        BRIEFING_ROLL_THREE_TELEPORT_BASE,
        GAMMA_REVERSE_TO_COUNTER_CLOCKWISE,
        GAMMA_TO_BETA,
        NONE
    }

    public MysteryTeleportEvent(Color playerColor, String pieceName, String targetDestinationName,
                                String targetCellId, TeleportEffect effect) {
        this(playerColor, pieceName, targetDestinationName, targetCellId, effect, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
