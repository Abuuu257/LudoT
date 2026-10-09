package com.apiit.ludot.domain.model.states;

import com.apiit.ludot.domain.model.Piece;

public class HomeStraightState implements PieceState {

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
        return true;
    }

    @Override
    public boolean isOnTrack() {
        return false;
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public String getStateName() {
        return "HomeStraight";
    }

    @Override
    public void onRoundEnd(Piece piece) {
        piece.decrementAuraRounds();
    }
}
