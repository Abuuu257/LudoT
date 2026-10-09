package com.apiit.ludot.patterns.command;

import com.apiit.ludot.usecases.dto.MoveResultDTO;

public class NullCommand implements GameCommand {
    private final String reason;
    private final MoveResultDTO result;

    public NullCommand(String reason) {
        this.reason = reason;
        this.result = MoveResultDTO.failed(reason);
    }

    @Override
    public void execute() {

    }

    @Override
    public String getDescription() {
        return "NullCommand: " + reason;
    }

    @Override
    public MoveResultDTO getResult() {
        return result;
    }
}
