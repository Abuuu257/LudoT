package com.apiit.ludot.domain.service;

import com.apiit.ludot.domain.model.Direction;

import java.util.Random;

public class StandardCoin implements ICoin {
    private final Random random;

    public StandardCoin() {
        this.random = new Random();
    }

    public StandardCoin(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Direction toss() {
        return random.nextBoolean() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
    }
}
