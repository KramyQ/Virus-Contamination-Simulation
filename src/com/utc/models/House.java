package com.utc.models;

import sim.engine.Schedule;

public class House extends Location {
    public House(Integer x, Integer y, Integer id, Boolean maskRequired, Boolean openAir, Integer capacity, Integer maxStayTime, LocationTypeEnum type, Schedule schedule, Occupancy OccupancyLabel) {
        super(x, y, id, maskRequired, openAir, capacity, maxStayTime, type, schedule, OccupancyLabel);
    }

    @Override
    public void updateLabel() {

    }
}
