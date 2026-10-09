package com.apiit.ludot.adapters.controller;

import com.apiit.ludot.adapters.presenter.ConsolePresenter;
import com.apiit.ludot.patterns.facade.LudoGameFacade;

public class SimulationController {
    private final LudoGameFacade facade;
    private final ConsolePresenter presenter;

    public SimulationController(boolean verbose) {
        this.presenter = new ConsolePresenter(verbose);
        this.facade = new LudoGameFacade();
        this.facade.addEventListener(presenter);
    }

    public void startSimulation() {
        facade.runGame();
    }
}
