package com.utc.models;

public class Occupancy {
    private final Integer MaxNumberOfOccupants;
    private Integer NumberOfOccupants;

    public Occupancy(Integer NumberOfOccupants, Integer MaxNumberOfOccupants) {
        this.NumberOfOccupants = NumberOfOccupants;
        this.MaxNumberOfOccupants = MaxNumberOfOccupants;
    }

    public Integer getNumberOfOccupants() {
        return NumberOfOccupants;
    }

    public void setNumberOfOccupants(Integer numberOfOccupants) {
        NumberOfOccupants = numberOfOccupants;
    }

    public Integer getMaxNumberOfOccupants() {
        return MaxNumberOfOccupants;
    }
}
