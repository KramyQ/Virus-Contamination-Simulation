package com.utc.views;

import com.utc.Simulation;
import com.utc.models.*;
import sim.display.ChartUtilities;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

import javax.swing.*;
import java.awt.*;

public class SimulationWithUI extends GUIState {

    public static int FRAME_SIZE = 1000;
    public Simulation model;
    public Display2D display;
    public JFrame displayFrame;
    public sim.util.media.chart.TimeSeriesAttributes InfectedPopulation;
    public ChartUtilities Population;
    SparseGridPortrayal2D cityPortrayal = new SparseGridPortrayal2D();

    public SimulationWithUI(SimState state, Simulation model) {
        super(state);
        this.model = model;
    }

    public static String getName() {
        return "Virus simulation";
    }

    public void start() {
        super.start();
        setupPortrayals();
    }

    public Object getSimulationInspectedObject() {
        return this.model;
    }


    public void init(Controller c) {
        super.init(c);

        display = new Display2D(FRAME_SIZE, FRAME_SIZE, this);
        display.setClipping(false);
        displayFrame = display.createFrame();
        displayFrame.setTitle("Virus");
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        display.attach(this.getInspector(), "model");
        display.attach(cityPortrayal, "Yard");
    }

    public void setupPortrayals() {

        Simulation simulation = (Simulation) state;
        cityPortrayal.setField(Simulation.city);
        cityPortrayal.setPortrayalForClass(Location.class, getLocationPortrayal());
        cityPortrayal.setPortrayalForClass(Person.class, getPersonPortrayal());
        cityPortrayal.setPortrayalForClass(House.class, getHousePortrayal());
        cityPortrayal.setPortrayalForClass(Road.class, getRoadPortrayal());
        cityPortrayal.setPortrayalForClass(Occupancy.class, getOccupancyPortrayal());
        display.reset();
        display.setBackdrop(Color.black);
        display.repaint();


    }

    private Portrayal getLocationPortrayal() {
        RectanglePortrayal2D r = new RectanglePortrayal2D() {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Location location = (Location) object;
                paint = (location.getType() == LocationTypeEnum.HOSPITAL) ? new Color(0xFFFFFFFF, true) : new Color(0xCAB92D);
                filled = true;
                scale = 5;
                super.draw(object, graphics, info);
            }
        };
        return r;
    }

    private Portrayal getHousePortrayal() {
        RectanglePortrayal2D r = new RectanglePortrayal2D();
        r.paint = new Color(0x90064072, true);
        r.filled = true;
        r.scale = 5;
        return r;
    }

    private Portrayal getRoadPortrayal() {
        RectanglePortrayal2D r = new RectanglePortrayal2D();
        r.paint = new Color(0xFFAEA5A5, true);
        r.filled = false;
        r.scale = 1;
        return r;
    }

    private Portrayal getOccupancyPortrayal() {
        RectanglePortrayal2D x = new RectanglePortrayal2D();
        x.paint = new Color(0x0AEA5A5, true);
        LabelledPortrayal2D r = new LabelledPortrayal2D(x, "numberOfOccupants") {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Occupancy person = (Occupancy) object;
                scaley = 1;
                paint = new Color(0xFFFFFF);
                onlyLabelWhenSelected = false;
                label = ((Occupancy) object).getNumberOfOccupants() + "/" + ((Occupancy) object).getMaxNumberOfOccupants();
                super.draw(object, graphics, info);
            }
        };
        return r;
    }

    private Portrayal getPersonPortrayal() {
        OvalPortrayal2D r = new OvalPortrayal2D() {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Person person = (Person) object;
                paint = person.getSick() ? new Color(0xCA0707) : person.isImmune() ? new Color(0x1B8BE9) : new Color(0x80CA09);
                super.draw(object, graphics, info);
            }
        };
        r.filled = true;
        r.scale = 0.75;
        return r;
    }
}
