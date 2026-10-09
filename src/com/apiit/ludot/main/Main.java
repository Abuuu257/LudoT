package com.apiit.ludot.main;

import com.apiit.ludot.adapters.controller.SimulationController;

public class Main {
    public static void main(String[] args) {
        boolean verbose = false;
        for (String arg : args) {
            if ("--verbose".equalsIgnoreCase(arg) || "-v".equalsIgnoreCase(arg)) {
                verbose = true;
            }
        }
        SimulationController controller = new SimulationController(verbose);
        controller.startSimulation();
    }
}
