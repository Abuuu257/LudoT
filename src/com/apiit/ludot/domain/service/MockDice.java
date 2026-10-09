package com.apiit.ludot.domain.service;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class MockDice implements IDice {
    private final Queue<Integer> rolls;
    private int defaultRoll;

    public MockDice(Integer... sequence) {
        this.rolls = new ArrayDeque<>(Arrays.asList(sequence));
        this.defaultRoll = 6;
    }

    public void queueRolls(Integer... values) {
        rolls.addAll(Arrays.asList(values));
    }

    public void setDefaultRoll(int roll) {
        this.defaultRoll = roll;
    }

    @Override
    public int roll() {
        if (!rolls.isEmpty()) {
            return rolls.poll();
        }
        return defaultRoll;
    }
}
