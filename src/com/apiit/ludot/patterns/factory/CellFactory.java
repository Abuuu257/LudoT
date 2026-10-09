package com.apiit.ludot.patterns.factory;

import com.apiit.ludot.domain.model.Cell;
import com.apiit.ludot.domain.model.CellType;
import com.apiit.ludot.domain.model.Color;

public class CellFactory {

    private CellFactory() {

    }

    public static Cell createTrackCell(int trackIndex) {
        CellType type = CellType.STANDARD;
        Color associatedColor = null;

        for (Color color : Color.values()) {
            if (color.getStartXIndex() == trackIndex) {
                type = CellType.STARTING_X;
                associatedColor = color;
                break;
            } else if (color.getApproachIndex() == trackIndex) {
                type = CellType.APPROACH;
                associatedColor = color;
                break;
            }
        }
        return new Cell(String.valueOf(trackIndex), trackIndex, type, associatedColor);
    }

    public static Cell createHomeStraightCell(Color color, int straightIndex) {
        String cellId = color.getHomePathCellName(straightIndex);
        return new Cell(cellId, -1, CellType.HOME_STRAIGHT, color);
    }
}
