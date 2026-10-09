package com.apiit.ludot.patterns.observer;

import java.util.ArrayList;
import java.util.List;

public class GameEventPublisher {
    private final List<GameEventListener> listeners = new ArrayList<>();

    public void subscribe(GameEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public void clear() {
        listeners.clear();
    }
}
