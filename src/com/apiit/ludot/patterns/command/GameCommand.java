package com.apiit.ludot.patterns.command;

import com.apiit.ludot.usecases.dto.MoveResultDTO;

public interface GameCommand {
    void execute();
    String getDescription();
    MoveResultDTO getResult();
}
