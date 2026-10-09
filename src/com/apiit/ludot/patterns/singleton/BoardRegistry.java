package com.apiit.ludot.patterns.singleton;

import com.apiit.ludot.domain.model.Color;

public class BoardRegistry {

    public static final int TRACK_SIZE = 52;
    public static final int HOME_STRAIGHT_SIZE = 5;

    public static final int ALPHA_OFFSET = 9;
    public static final int BETA_OFFSET = 27;
    public static final int GAMMA_OFFSET = 46;

    private final int alphaCellIndex;
    private final int betaCellIndex;
    private final int gammaCellIndex;

    private BoardRegistry() {
        int yellowApproach = Color.YELLOW.getApproachIndex();
        this.alphaCellIndex = (yellowApproach + ALPHA_OFFSET) % TRACK_SIZE;
        this.betaCellIndex = (yellowApproach + BETA_OFFSET) % TRACK_SIZE;
        this.gammaCellIndex = (yellowApproach + GAMMA_OFFSET) % TRACK_SIZE;
    }

    private static class InstanceHolder {
        private static final BoardRegistry INSTANCE = new BoardRegistry();
    }

    public static BoardRegistry getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public int getAlphaCellIndex() {
        return alphaCellIndex;
    }

    public int getBetaCellIndex() {
        return betaCellIndex;
    }

    public int getGammaCellIndex() {
        return gammaCellIndex;
    }

    public int getStartXForColor(Color color) {
        return color.getStartXIndex();
    }

    public int getApproachForColor(Color color) {
        return color.getApproachIndex();
    }

    public int calculateTrackDistance(int fromCell, int toCell, boolean clockwise) {
        if (fromCell == toCell) {
            return 0;
        }
        if (clockwise) {
            return (toCell - fromCell + TRACK_SIZE) % TRACK_SIZE;
        } else {
            return (fromCell - toCell + TRACK_SIZE) % TRACK_SIZE;
        }
    }
}
