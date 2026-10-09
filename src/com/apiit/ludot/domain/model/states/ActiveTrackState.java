package com.apiit.ludot.domain.model.states;

import com.apiit.ludot.domain.model.Piece;

public class ActiveTrackState implements PieceState {

    @Override
    public boolean canMove() {
        return true;
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
        return false;
    }

    @Override
    public String getStateName() {
        return "ActiveTrack";
    }

    @Override
    public void onRoundEnd(Piece piece) {
        piece.decrementAuraRounds();
    }
}
