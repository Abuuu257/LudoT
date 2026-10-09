package com.apiit.ludot.usecases.dto;

public class MoveDecision {
    private final boolean hasMove;
    private final MoveCandidate candidate;
    private final String reason;

    private MoveDecision(boolean hasMove, MoveCandidate candidate, String reason) {
        this.hasMove = hasMove;
        this.candidate = candidate;
        this.reason = reason;
    }

    public static MoveDecision make(MoveCandidate candidate) {
        return new MoveDecision(true, candidate, "Move chosen");
    }

    public static MoveDecision none(String reason) {
        return new MoveDecision(false, null, reason);
    }

    public boolean hasMove() {
        return hasMove;
    }

    public MoveCandidate getCandidate() {
        return candidate;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return hasMove ? "MoveDecision[" + candidate.moveType() + " for " + candidate.piece().getId() + "]"
                       : "MoveDecision[NONE: " + reason + "]";
    }
}
