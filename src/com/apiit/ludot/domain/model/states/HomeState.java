package com.apiit.ludot.domain.model.states;

import com.apiit.ludot.domain.model.Piece;

public class HomeState implements PieceState {

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
        return true;
    }

    @Override
    public boolean isInHomeStraight() {
        return false;
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
        return "Home";
    }

    @Override
    public void onRoundEnd(Piece piece) {

    }
}
