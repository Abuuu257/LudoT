package com.apiit.ludot.domain.model.states;

import com.apiit.ludot.domain.model.Piece;

public interface PieceState {
    boolean canMove();
    boolean isAtBase();
    boolean isInHome();
    boolean isInHomeStraight();
    boolean isOnTrack();
    boolean isRestricted();
    String getStateName();
    void onRoundEnd(Piece piece);
}
