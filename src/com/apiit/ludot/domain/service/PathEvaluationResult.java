package com.apiit.ludot.domain.service;

import com.apiit.ludot.domain.model.Blockade;

public class PathEvaluationResult {
    private final boolean valid;
    private final boolean blocked;
    private final boolean reachesHome;
    private final boolean entersHomeStraight;

    private final String fromCellId;
    private final String intendedTargetCellId;
    private final String actualTargetCellId;
    private final int targetTrackIndex;
    private final int targetHomeStraightIndex;
    private final int cellBeforeBlockTrackIndex;
    private final Blockade blockingBlockade;
    private final boolean passedApproach;
    private final String failureReason;

    private PathEvaluationResult(boolean valid, boolean blocked, boolean reachesHome, boolean entersHomeStraight,
                                 String fromCellId, String intendedTargetCellId, String actualTargetCellId,
                                 int targetTrackIndex, int targetHomeStraightIndex, int cellBeforeBlockTrackIndex,
                                 Blockade blockingBlockade, boolean passedApproach, String failureReason) {
        this.valid = valid;
        this.blocked = blocked;
        this.reachesHome = reachesHome;
        this.entersHomeStraight = entersHomeStraight;
        this.fromCellId = fromCellId;
        this.intendedTargetCellId = intendedTargetCellId;
        this.actualTargetCellId = actualTargetCellId;
        this.targetTrackIndex = targetTrackIndex;
        this.targetHomeStraightIndex = targetHomeStraightIndex;
        this.cellBeforeBlockTrackIndex = cellBeforeBlockTrackIndex;
        this.blockingBlockade = blockingBlockade;
        this.passedApproach = passedApproach;
        this.failureReason = failureReason;
    }

    public static PathEvaluationResult illegal(String reason) {
        return new PathEvaluationResult(false, false, false, false,
                "", "", "", -1, -1, -1, null, false, reason);
    }

    public static PathEvaluationResult blocked(String fromCell, String intendedCell, String cellBeforeBlock,
                                               int cellBeforeBlockIndex, Blockade blockade) {
        return new PathEvaluationResult(false, true, false, false,
                fromCell, intendedCell, cellBeforeBlock, cellBeforeBlockIndex, -1,
                cellBeforeBlockIndex, blockade, false, "Blocked by opponent blockade");
    }

    public static PathEvaluationResult validTrackMove(String fromCell, String toCell, int toTrackIndex, boolean passedApproach) {
        return new PathEvaluationResult(true, false, false, false,
                fromCell, toCell, toCell, toTrackIndex, -1, -1, null, passedApproach, null);
    }

    public static PathEvaluationResult validHomeStraightEntry(String fromCell, String toCell, int straightIndex, boolean passedApproach) {
        return new PathEvaluationResult(true, false, false, true,
                fromCell, toCell, toCell, -1, straightIndex, -1, null, passedApproach, null);
    }

    public static PathEvaluationResult validHomeStraightMove(String fromCell, String toCell, int straightIndex, boolean reachesHome) {
        return new PathEvaluationResult(true, false, reachesHome, !reachesHome,
                fromCell, toCell, toCell, -1, reachesHome ? -1 : straightIndex, -1, null, false, null);
    }

    public static PathEvaluationResult validReachHome(String fromCell, String toCell, boolean passedApproach) {
        return new PathEvaluationResult(true, false, true, false,
                fromCell, toCell, toCell, -1, -1, -1, null, passedApproach, null);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isReachesHome() {
        return reachesHome;
    }

    public boolean isEntersHomeStraight() {
        return entersHomeStraight;
    }

    public String getFromCellId() {
        return fromCellId;
    }

    public String getIntendedTargetCellId() {
        return intendedTargetCellId;
    }

    public String getActualTargetCellId() {
        return actualTargetCellId;
    }

    public int getTargetTrackIndex() {
        return targetTrackIndex;
    }

    public int getTargetHomeStraightIndex() {
        return targetHomeStraightIndex;
    }

    public int getCellBeforeBlockTrackIndex() {
        return cellBeforeBlockTrackIndex;
    }

    public Blockade getBlockingBlockade() {
        return blockingBlockade;
    }

    public boolean isPassedApproach() {
        return passedApproach;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
