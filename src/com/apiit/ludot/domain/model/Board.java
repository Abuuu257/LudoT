package com.apiit.ludot.domain.model;

import com.apiit.ludot.patterns.singleton.BoardRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Board {
    private final Map<Integer, Cell> trackCells;
    private final Map<Color, List<Cell>> homeStraightCells;
    private final Map<Color, List<Piece>> baseAreas;
    private final Map<Color, List<Piece>> homeAreas;

    private int mysteryCellLocation;
    private int mysteryRoundsRemaining;
    private int previousMysteryLocation;
    private int roundsWithPiecesOnTrack;
    private boolean mysteryCellActive;

    public Board() {
        this.trackCells = new LinkedHashMap<>();
        this.homeStraightCells = new EnumMap<>(Color.class);
        this.baseAreas = new EnumMap<>(Color.class);
        this.homeAreas = new EnumMap<>(Color.class);
        this.mysteryCellLocation = -1;
        this.mysteryRoundsRemaining = 0;
        this.previousMysteryLocation = -1;
        this.roundsWithPiecesOnTrack = 0;
        this.mysteryCellActive = false;

        initializeCells();
    }

    private void initializeCells() {

        for (int i = 0; i < BoardRegistry.TRACK_SIZE; i++) {
            CellType type = CellType.STANDARD;
            Color associatedColor = null;

            for (Color color : Color.values()) {
                if (color.getStartXIndex() == i) {
                    type = CellType.STARTING_X;
                    associatedColor = color;
                    break;
                } else if (color.getApproachIndex() == i) {
                    type = CellType.APPROACH;
                    associatedColor = color;
                    break;
                }
            }
            trackCells.put(i, new Cell(String.valueOf(i), i, type, associatedColor));
        }

        for (Color color : Color.values()) {
            List<Cell> straights = new ArrayList<>();
            for (int i = 0; i < BoardRegistry.HOME_STRAIGHT_SIZE; i++) {
                String id = color.getHomePathCellName(i);
                straights.add(new Cell(id, -1, CellType.HOME_STRAIGHT, color));
            }
            homeStraightCells.put(color, straights);
            baseAreas.put(color, new ArrayList<>());
            homeAreas.put(color, new ArrayList<>());
        }
    }

    public Cell getTrackCell(int trackIndex) {
        int normalized = (trackIndex % BoardRegistry.TRACK_SIZE + BoardRegistry.TRACK_SIZE) % BoardRegistry.TRACK_SIZE;
        return trackCells.get(normalized);
    }

    public Cell getHomeStraightCell(Color color, int index) {
        if (index < 0 || index >= BoardRegistry.HOME_STRAIGHT_SIZE) {
            return null;
        }
        return homeStraightCells.get(color).get(index);
    }

    public List<Cell> getAllTrackCells() {
        return Collections.unmodifiableList(new ArrayList<>(trackCells.values()));
    }

    public Blockade getBlockadeAt(int trackIndex) {
        Cell cell = getTrackCell(trackIndex);
        if (cell != null && cell.isBlockade()) {
            return new Blockade(cell.getBlockadeOwner(), trackIndex, cell.getOccupants());
        }
        return null;
    }

    public boolean hasAnyPieceOnTrack() {
        for (Cell cell : trackCells.values()) {
            if (!cell.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getMysteryCellLocation() {
        return mysteryCellLocation;
    }

    public int getMysteryRoundsRemaining() {
        return mysteryRoundsRemaining;
    }

    public boolean isMysteryCellActive() {
        return mysteryCellActive;
    }

    public boolean checkAndTriggerMysterySpawn(Random random) {
        if (hasAnyPieceOnTrack()) {
            roundsWithPiecesOnTrack++;
        }

        if (!mysteryCellActive) {
            if (roundsWithPiecesOnTrack >= 2) {
                return spawnMysteryCell(random);
            }
        } else {

            mysteryRoundsRemaining--;
            if (mysteryRoundsRemaining <= 0) {
                despawnMysteryCell();

                return spawnMysteryCell(random);
            }
        }
        return false;
    }

    public boolean spawnMysteryCell(Random random) {
        List<Integer> candidateIndices = new ArrayList<>();
        for (int i = 0; i < BoardRegistry.TRACK_SIZE; i++) {
            Cell cell = trackCells.get(i);
            if (cell.isEmpty() && i != previousMysteryLocation) {
                candidateIndices.add(i);
            }
        }

        if (candidateIndices.isEmpty()) {

            for (int i = 0; i < BoardRegistry.TRACK_SIZE; i++) {
                if (trackCells.get(i).isEmpty()) {
                    candidateIndices.add(i);
                }
            }
        }

        if (!candidateIndices.isEmpty()) {
            int selectedIndex = candidateIndices.get(random.nextInt(candidateIndices.size()));
            despawnMysteryCell();

            mysteryCellLocation = selectedIndex;
            previousMysteryLocation = selectedIndex;
            mysteryRoundsRemaining = 4;
            mysteryCellActive = true;
            trackCells.get(selectedIndex).setMysteryCell(true);
            return true;
        }
        return false;
    }

    public void despawnMysteryCell() {
        if (mysteryCellLocation >= 0) {
            Cell oldCell = trackCells.get(mysteryCellLocation);
            if (oldCell != null) {
                oldCell.setMysteryCell(false);
            }
        }
        mysteryCellActive = false;
        mysteryCellLocation = -1;
        mysteryRoundsRemaining = 0;
    }

    public void setMysteryCellManually(int location, int rounds) {
        despawnMysteryCell();
        mysteryCellLocation = location;
        previousMysteryLocation = location;
        mysteryRoundsRemaining = rounds;
        mysteryCellActive = true;
        trackCells.get(location).setMysteryCell(true);
    }
}
