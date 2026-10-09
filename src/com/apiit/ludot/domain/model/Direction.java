package com.apiit.ludot.domain.model;

public enum Direction {
    CLOCKWISE("clockwise", 1),
    COUNTER_CLOCKWISE("counterclockwise", -1);

    private final String displayName;
    private final int stepDelta;

    Direction(String displayName, int stepDelta) {
        this.displayName = displayName;
        this.stepDelta = stepDelta;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStepDelta() {
        return stepDelta;
    }

    public Direction getOpposite() {
        return this == CLOCKWISE ? COUNTER_CLOCKWISE : CLOCKWISE;
    }
}
