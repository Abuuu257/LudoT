package com.apiit.ludot.patterns.nullobject;

import com.apiit.ludot.usecases.dto.MoveDecision;

public final class NullMove {
    private static final MoveDecision INSTANCE = MoveDecision.none("No valid move available");

    private NullMove() {}

    public static MoveDecision getInstance() {
        return INSTANCE;
    }
}
