package com.apiit.ludot.domain.model;

public enum Color {
    RED("red", "R", 26, 25),
    GREEN("green", "G", 39, 38),
    YELLOW("yellow", "Y", 0, 51),
    BLUE("blue", "B", 13, 12);

    private final String displayName;
    private final String shortPrefix;
    private final int startXIndex;
    private final int approachIndex;

    Color(String displayName, String shortPrefix, int startXIndex, int approachIndex) {
        this.displayName = displayName;
        this.shortPrefix = shortPrefix;
        this.startXIndex = startXIndex;
        this.approachIndex = approachIndex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCapitalizedName() {
        return Character.toUpperCase(displayName.charAt(0)) + displayName.substring(1);
    }

    public String getShortPrefix() {
        return shortPrefix;
    }

    public int getStartXIndex() {
        return startXIndex;
    }

    public int getApproachIndex() {
        return approachIndex;
    }

    public String getHomePathCellName(int cellNumber) {
        return displayName.toLowerCase() + "homepath" + cellNumber;
    }

    public Color getNextClockwise() {
        return switch (this) {
            case RED -> GREEN;
            case GREEN -> YELLOW;
            case YELLOW -> BLUE;
            case BLUE -> RED;
        };
    }
}
