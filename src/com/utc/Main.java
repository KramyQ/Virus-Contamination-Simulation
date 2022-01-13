package com.utc;


import com.utc.views.SimulationWithUI;
import sim.display.Console;

public class Main {
    public static void main(String[] args) {
        runUI();
    }

    public static void runUI() {

        Simulation model = new Simulation(System.currentTimeMillis());
        SimulationWithUI gui = new SimulationWithUI(model, model);
        Console console = new Console(gui);
        console.setVisible(true);
    }
}
