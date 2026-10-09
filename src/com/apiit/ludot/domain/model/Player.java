package com.apiit.ludot.domain.model;

import com.apiit.ludot.patterns.strategy.PlayerStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Player {
    private final Color color;
    private final List<Piece> pieces;
    private PlayerStrategy strategy;
    private int consecutiveSixes;
    private int consecutiveThrees;
    private int moveCount;

    public Player(Color color, PlayerStrategy strategy) {
        this.color = Objects.requireNonNull(color, "color must not be null");
        this.strategy = strategy;
        this.pieces = new ArrayList<>(4);
        for (int i = 1; i <= 4; i++) {
            this.pieces.add(new Piece(color, i));
        }
        this.consecutiveSixes = 0;
        this.consecutiveThrees = 0;
        this.moveCount = 0;
    }

    public Color getColor() {
        return color;
    }

    public List<Piece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    public Piece getPiece(int number) {
        if (number < 1 || number > 4) {
            return null;
        }
        return pieces.get(number - 1);
    }

    public PlayerStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(PlayerStrategy strategy) {
        this.strategy = strategy;
    }

    public int getConsecutiveSixes() {
        return consecutiveSixes;
    }

    public void incrementConsecutiveSixes() {
        this.consecutiveSixes++;
    }

    public void resetConsecutiveSixes() {
        this.consecutiveSixes = 0;
    }

    public int getConsecutiveThrees() {
        return consecutiveThrees;
    }

    public void incrementConsecutiveThrees() {
        this.consecutiveThrees++;
    }

    public void resetConsecutiveThrees() {
        this.consecutiveThrees = 0;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void incrementMoveCount() {
        this.moveCount++;
    }

    public List<Piece> getPiecesInBase() {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.getState().isAtBase()) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Piece> getPiecesOnBoard() {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.getState().isOnTrack() || p.getState().isInHomeStraight()) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Piece> getPiecesInHome() {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.getState().isInHome()) {
                result.add(p);
            }
        }
        return result;
    }

    public boolean isWinner() {
        return getPiecesInHome().size() == 4;
    }

    public boolean hasBlockade(Board board) {
        for (Cell cell : board.getAllTrackCells()) {
            if (cell.isBlockade() && cell.getBlockadeOwner() == color) {
                return true;
            }
        }
        return false;
    }

    public List<Blockade> getBlockades(Board board) {
        List<Blockade> blockades = new ArrayList<>();
        for (Cell cell : board.getAllTrackCells()) {
            if (cell.isBlockade() && cell.getBlockadeOwner() == color) {
                blockades.add(new Blockade(color, cell.getTrackIndex(), cell.getOccupants()));
            }
        }
        return blockades;
    }

    public boolean hasPieceInBriefing() {
        for (Piece p : pieces) {
            if (p.getState().isRestricted()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return color.getCapitalizedName() + "Player[pieces=" + pieces.size() + "]";
    }
}
