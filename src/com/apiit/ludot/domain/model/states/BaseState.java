package com.apiit.ludot.domain.model.states;

import com.apiit.ludot.domain.model.Piece;

public class BaseState implements PieceState {

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public boolean isAtBase() {
        return true;
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
        return false;
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public String getStateName() {
        return "Base";
    }

    @Override
    public void onRoundEnd(Piece piece) {

    }
}
