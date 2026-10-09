package com.apiit.ludot.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cell {
    private final String cellId;
    private final int trackIndex;
    private final CellType cellType;
    private final Color associatedColor;
    private final List<Piece> occupants;
    private boolean isMysteryCell;

    public Cell(String cellId, int trackIndex, CellType cellType, Color associatedColor) {
        this.cellId = Objects.requireNonNull(cellId, "cellId must not be null");
        this.trackIndex = trackIndex;
        this.cellType = Objects.requireNonNull(cellType, "cellType must not be null");
        this.associatedColor = associatedColor;
        this.occupants = new ArrayList<>();
        this.isMysteryCell = false;
    }

    public String getCellId() {
        return cellId;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public CellType getCellType() {
        return cellType;
    }

    public Color getAssociatedColor() {
        return associatedColor;
    }

    public List<Piece> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public boolean isMysteryCell() {
        return isMysteryCell;
    }

    public void setMysteryCell(boolean mysteryCell) {
        isMysteryCell = mysteryCell;
    }

    public void addOccupant(Piece piece) {
        if (!occupants.contains(piece)) {
            occupants.add(piece);
        }
    }

    public void removeOccupant(Piece piece) {
        occupants.remove(piece);
    }

    public void clearOccupants() {
        occupants.clear();
    }

    public boolean isEmpty() {
        return occupants.isEmpty();
    }

    public int getOccupantCount() {
        return occupants.size();
    }

    public boolean isBlockade() {
        if (occupants.size() < 2) {
            return false;
        }
        Color owner = occupants.get(0).getColor();
        for (Piece p : occupants) {
            if (p.getColor() != owner) {
                return false;
            }
        }
        return true;
    }

    public Color getBlockadeOwner() {
        if (isBlockade()) {
            return occupants.get(0).getColor();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Cell[" + cellId + ", occupants=" + occupants.size() + (isMysteryCell ? ", MYSTERY" : "") + "]";
    }
}
