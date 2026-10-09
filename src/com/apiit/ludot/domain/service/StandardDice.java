package com.apiit.ludot.domain.service;

import java.util.Random;

public class StandardDice implements IDice {
    private final Random random;

    public StandardDice() {
        this.random = new Random();
    }

    public StandardDice(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public int roll() {
        return random.nextInt(6) + 1;
    }
}
