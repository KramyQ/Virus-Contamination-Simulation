package com.utc.models;

import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Person extends GridObject implements Steppable {

    private Integer id;
    private Integer age;
    private Boolean comorbidities;

    private List<TaskHistory> history;

    private boolean readyToMove;

    private Integer severity;

    private Location home;

    private boolean immune;

    private Integer sicknessStart;

    private Location currentLocation;
    private Boolean isSick;
    private float coefR = 0;

    public Person(Integer x, Integer y) {
        super(x, y);
    }

    public Person(Integer x, Integer y, Integer id, Integer age, Integer severity, Boolean comorbidities, Location home, Location currentLocation, Boolean isSick) {
        super(x, y);
        this.id = id;
        this.age = age;
        this.comorbidities = comorbidities;
        this.home = home;
        this.currentLocation = currentLocation;
        this.history = new ArrayList<>();
        this.isSick = Objects.nonNull(isSick) ? isSick : false;
        this.severity = severity;
        this.readyToMove = true;
        this.immune = false;
        this.sicknessStart = 0;
    }

    public float getCoefR() {
        return coefR;
    }

    public void setCoefR(float coefR) {
        this.coefR = coefR;
    }

    @Override
    public void step(SimState simState) {

    }

    public Location getHome() {
        return home;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getComorbidities() {
        return comorbidities;
    }

    public void setComorbidities(Boolean comorbidities) {
        this.comorbidities = comorbidities;
    }

    public Boolean getSick() {
        return isSick;
    }

    public void setSick(Boolean sick) {
        isSick = sick;
    }

    public Integer getSeverity() {
        return severity;
    }

    public List<TaskHistory> getTaskHistory() {
        return history;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public List<TaskHistory> getHistory() {
        return history;
    }

    public boolean isReadyToMove() {
        return readyToMove;
    }

    public void setReadyToMove(boolean readyToMove) {
        this.readyToMove = readyToMove;
    }

    public boolean isImmune() {
        return immune;
    }

    public void setImmune(boolean immune) {
        this.immune = immune;
    }

    public Integer getSicknessStart() {
        return sicknessStart;
    }

    public void setSicknessStart(Integer sicknessStart) {
        this.sicknessStart = sicknessStart;
    }
}

