package com.apiit.ludot.domain.model.states;

import com.apiit.ludot.domain.model.Piece;

public class BriefingState implements PieceState {

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public boolean isAtBase() {
        return false;
    }

    @Override
    public boolean isInHome() {
        return false;
    }

    @Override
    public boolean isInHomeStraight() {
        return false;
    }

    @Override
    public boolean isOnTrack() {
        return true;
    }

    @Override
    public boolean isRestricted() {
        return true;
    }

    @Override
    public String getStateName() {
        return "Briefing";
    }

    @Override
    public void onRoundEnd(Piece piece) {
        piece.decrementBriefingRounds();
        piece.decrementAuraRounds();
    }
}
