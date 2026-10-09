package com.apiit.ludot.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Blockade {
    private final Color ownerColor;
    private final int trackIndex;
    private final List<Piece> pieces;

    public Blockade(Color ownerColor, int trackIndex, List<Piece> pieces) {
        this.ownerColor = Objects.requireNonNull(ownerColor, "ownerColor must not be null");
        this.trackIndex = trackIndex;
        if (pieces == null || pieces.size() < 2) {
            throw new IllegalArgumentException("A blockade must consist of at least 2 pieces");
        }
        this.pieces = new ArrayList<>(pieces);
    }

    public Color getOwnerColor() {
        return ownerColor;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public List<Piece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    public int size() {
        return pieces.size();
    }

    public int calculateBlockMoveDistance(int diceRoll) {
        return diceRoll / pieces.size();
    }

    public Direction determineBlockDirection(int clockwiseDistanceToHome, int counterClockwiseDistanceToHome) {
        boolean hasClockwise = false;
        boolean hasCounterClockwise = false;

        for (Piece p : pieces) {
            if (p.getCurrentDirection() == Direction.CLOCKWISE) {
                hasClockwise = true;
            } else if (p.getCurrentDirection() == Direction.COUNTER_CLOCKWISE) {
                hasCounterClockwise = true;
            }
        }

        if (hasClockwise && hasCounterClockwise) {

            return clockwiseDistanceToHome >= counterClockwiseDistanceToHome
                    ? Direction.CLOCKWISE
                    : Direction.COUNTER_CLOCKWISE;
        }

        return hasClockwise ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
    }

    public boolean canCapture(Blockade targetBlockade) {
        return targetBlockade != null
                && this.ownerColor != targetBlockade.getOwnerColor()
                && this.size() == targetBlockade.size();
    }

    @Override
    public String toString() {
        return "Blockade[" + ownerColor + " at cell " + trackIndex + ", pieces=" + pieces.size() + "]";
    }
}
