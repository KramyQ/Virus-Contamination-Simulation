package com.utc.models;

public class TaskHistory {
    private final Location location;
    private final Integer timestamp;

    public TaskHistory(Location location, Integer timestamp) {
        this.location = location;
        this.timestamp = timestamp;
    }

    public Location getLocation() {
        return location;
    }

    public Integer getTimeStamp() {
        return timestamp;
    }

}
