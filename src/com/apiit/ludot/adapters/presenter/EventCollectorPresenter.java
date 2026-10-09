package com.apiit.ludot.adapters.presenter;

import com.apiit.ludot.patterns.observer.GameEvent;
import com.apiit.ludot.patterns.observer.GameEventListener;
import com.apiit.ludot.patterns.observer.events.MysteryTeleportEvent;
import com.apiit.ludot.patterns.observer.events.PieceCapturedEvent;
import com.apiit.ludot.patterns.observer.events.PieceSpawnedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventCollectorPresenter implements GameEventListener {
    private final List<GameEvent> collectedEvents = new ArrayList<>();
    private int captureCount = 0;
    private int mysteryTeleportCount = 0;
    private int spawnCount = 0;

    @Override
    public void onEvent(GameEvent event) {
        collectedEvents.add(event);
        if (event instanceof PieceCapturedEvent) {
            captureCount++;
        } else if (event instanceof MysteryTeleportEvent) {
            mysteryTeleportCount++;
        } else if (event instanceof PieceSpawnedEvent) {
            spawnCount++;
        }
    }

    public List<GameEvent> getCollectedEvents() {
        return Collections.unmodifiableList(collectedEvents);
    }

    public int getCaptureCount() {
        return captureCount;
    }

    public int getMysteryTeleportCount() {
        return mysteryTeleportCount;
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public boolean hasEventOfType(Class<? extends GameEvent> type) {
        for (GameEvent e : collectedEvents) {
            if (type.isInstance(e)) return true;
        }
        return false;
    }

    public List<GameEvent> getEventsOfType(Class<? extends GameEvent> type) {
        List<GameEvent> result = new ArrayList<>();
        for (GameEvent e : collectedEvents) {
            if (type.isInstance(e)) result.add(e);
        }
        return result;
    }

    public void clear() {
        collectedEvents.clear();
        captureCount = 0;
        mysteryTeleportCount = 0;
        spawnCount = 0;
    }
}
