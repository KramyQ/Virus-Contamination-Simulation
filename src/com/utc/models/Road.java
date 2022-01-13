package com.utc.models;

public class Road {
    private final Integer RoadSection;

    public Road(Integer RoadSec) {
        this.RoadSection = RoadSec;
    }

    @Override
    public String toString() {
        return "ROAD SECTION" + this.RoadSection;
    }
}
