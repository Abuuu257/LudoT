package com.apiit.ludot.domain.model;

import com.apiit.ludot.domain.model.states.ActiveTrackState;
import com.apiit.ludot.domain.model.states.BaseState;
import com.apiit.ludot.domain.model.states.BriefingState;
import com.apiit.ludot.domain.model.states.HomeState;
import com.apiit.ludot.domain.model.states.HomeStraightState;
import com.apiit.ludot.domain.model.states.PieceState;

import java.util.Objects;

public class Piece {
    private final String id;
    private final int pieceNumber;
    private final Color color;

    private PieceState state;
    private Direction currentDirection;
    private Direction originalDirection;
    private String currentCellId;
    private int trackIndex;
    private int homeStraightIndex;

    private int captureCount;
    private int approachPassCount;

    private AuraEffect auraEffect;
    private int auraRoundsRemaining;
    private int briefingRoundsRemaining;

    public Piece(Color color, int pieceNumber) {
        this.color = Objects.requireNonNull(color, "Color must not be null");
        this.pieceNumber = pieceNumber;
        this.id = color.getShortPrefix() + pieceNumber;
        resetToBase();
    }

    public final void resetToBase() {
        this.state = new BaseState();
        this.currentDirection = Direction.CLOCKWISE;
        this.originalDirection = Direction.CLOCKWISE;
        this.currentCellId = "Base";
        this.trackIndex = -1;
        this.homeStraightIndex = -1;
        this.captureCount = 0;
        this.approachPassCount = 0;
        this.auraEffect = AuraEffect.NONE;
        this.auraRoundsRemaining = 0;
        this.briefingRoundsRemaining = 0;
    }

    public void spawnToStart(Direction coinTossDirection) {
        this.state = new ActiveTrackState();
        this.currentDirection = coinTossDirection;
        this.originalDirection = coinTossDirection;
        this.trackIndex = color.getStartXIndex();
        this.currentCellId = String.valueOf(this.trackIndex);
        this.homeStraightIndex = -1;
        this.approachPassCount = 0;
    }

    public void moveToTrackCell(int newTrackIndex) {
        this.state = new ActiveTrackState();
        this.trackIndex = newTrackIndex;
        this.currentCellId = String.valueOf(newTrackIndex);
        this.homeStraightIndex = -1;
    }

    public void enterHomeStraight(int straightIndex) {
        this.state = new HomeStraightState();
        this.trackIndex = -1;
        this.homeStraightIndex = straightIndex;
        this.currentCellId = color.getHomePathCellName(straightIndex);
    }

    public void enterHome() {
        this.state = new HomeState();
        this.trackIndex = -1;
        this.homeStraightIndex = -1;
        this.currentCellId = "Home";
    }

    public void attendBriefing(int betaCellIndex) {
        this.state = new BriefingState();
        this.trackIndex = betaCellIndex;
        this.currentCellId = String.valueOf(betaCellIndex);
        this.homeStraightIndex = -1;
        this.briefingRoundsRemaining = 4;
    }

    public void decrementBriefingRounds() {
        if (briefingRoundsRemaining > 0) {
            briefingRoundsRemaining--;
            if (briefingRoundsRemaining == 0 && state.isRestricted()) {
                this.state = new ActiveTrackState();
            }
        }
    }

    public void applyAlphaEffect(AuraEffect effect) {
        this.auraEffect = effect;
        this.auraRoundsRemaining = 4;
    }

    public void decrementAuraRounds() {
        if (auraRoundsRemaining > 0) {
            auraRoundsRemaining--;
            if (auraRoundsRemaining == 0) {
                this.auraEffect = AuraEffect.NONE;
            }
        }
    }

    public int calculateEffectiveMovement(int diceRoll) {
        if (auraEffect == AuraEffect.ENERGIZED) {
            return diceRoll * 2;
        } else if (auraEffect == AuraEffect.SICK) {
            return Math.max(1, diceRoll / 2);
        }
        return diceRoll;
    }

    public void incrementCaptureCount() {
        this.captureCount++;
    }

    public void incrementApproachPassCount() {
        this.approachPassCount++;
    }

    public boolean canEnterHomeStraight() {
        if (captureCount < 1) {
            return false;
        }
        if (currentDirection == Direction.COUNTER_CLOCKWISE) {

            return approachPassCount >= 2;
        }

        return approachPassCount >= 1;
    }

    public void restoreOriginalDirection() {
        this.currentDirection = this.originalDirection;
    }

    public String getId() {
        return id;
    }

    public int getPieceNumber() {
        return pieceNumber;
    }

    public Color getColor() {
        return color;
    }

    public PieceState getState() {
        return state;
    }

    public void setState(PieceState state) {
        this.state = state;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public Direction getOriginalDirection() {
        return originalDirection;
    }

    public String getCurrentCellId() {
        return currentCellId;
    }

    public void setCurrentCellId(String currentCellId) {
        this.currentCellId = currentCellId;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public int getHomeStraightIndex() {
        return homeStraightIndex;
    }

    public int getCaptureCount() {
        return captureCount;
    }

    public int getApproachPassCount() {
        return approachPassCount;
    }

    public AuraEffect getAuraEffect() {
        return auraEffect;
    }

    public int getAuraRoundsRemaining() {
        return auraRoundsRemaining;
    }

    public int getBriefingRoundsRemaining() {
        return briefingRoundsRemaining;
    }

    @Override
    public String toString() {
        return id + "{" + currentCellId + ", " + currentDirection + ", captures=" + captureCount + "}";
    }
}
