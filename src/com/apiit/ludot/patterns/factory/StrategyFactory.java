package com.apiit.ludot.patterns.factory;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.patterns.strategy.BlueMysteryStrategy;
import com.apiit.ludot.patterns.strategy.GreenBlockerStrategy;
import com.apiit.ludot.patterns.strategy.NullPlayerStrategy;
import com.apiit.ludot.patterns.strategy.PlayerStrategy;
import com.apiit.ludot.patterns.strategy.RedAggressiveStrategy;
import com.apiit.ludot.patterns.strategy.YellowRacerStrategy;

public class StrategyFactory {

    private StrategyFactory() {

    }

    public static PlayerStrategy createStrategyForColor(Color color) {
        if (color == null) {
            return new NullPlayerStrategy();
        }
        return switch (color) {
            case RED -> new RedAggressiveStrategy();
            case GREEN -> new GreenBlockerStrategy();
            case YELLOW -> new YellowRacerStrategy();
            case BLUE -> new BlueMysteryStrategy();
        };
    }
}
