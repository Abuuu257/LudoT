package com.apiit.ludot.domain.service;

import com.apiit.ludot.domain.model.Direction;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class MockCoin implements ICoin {
    private final Queue<Direction> outcomes;
    private Direction defaultDirection;

    public MockCoin(Direction... sequence) {
        this.outcomes = new ArrayDeque<>(Arrays.asList(sequence));
        this.defaultDirection = Direction.CLOCKWISE;
    }

    public void queueDirections(Direction... directions) {
        outcomes.addAll(Arrays.asList(directions));
    }

    public void setDefaultDirection(Direction direction) {
        this.defaultDirection = direction;
    }

    @Override
    public Direction toss() {
        if (!outcomes.isEmpty()) {
            return outcomes.poll();
        }
        return defaultDirection;
    }
}
