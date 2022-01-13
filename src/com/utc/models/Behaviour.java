package com.utc.models;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.SparseGrid2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static com.utc.constants.AppConstants.CLEAR_TASK_HISTORY;

public class Behaviour implements Steppable {
    private final ArrayList<Person> population;
    private final ArrayList<Location> buildings;
    private final HashMap<Person, Location> movingPersons;
    private final SparseGrid2D city;
    private final Schedule schedule;
    private Integer numberOfCitizen = 0;
    private Integer numberOfCitizenInfected = 0;
    private Integer numberOfUninfectedCitizen = 0;
    private Integer numberOfImmuneCitizen = 0;
    private Integer numberOfDeads = 0;
    private float sumOfAverageR = 0;
    private float averageR = 0;

    public Behaviour(SparseGrid2D city, Schedule schedule) {
        this.city = city;
        this.population = new ArrayList<>();
        this.schedule = schedule;
        this.buildings = new ArrayList<>();
        this.movingPersons = new HashMap<>();
    }

    @Override
    public void step(SimState simState) {
        giveBehaviour();
        movePeople();
        refreshMoveColorImmunityKill();
    }

    private void giveBehaviour() {
        population.forEach((pers) -> {
            if (pers.isReadyToMove()) {
                movingPersons.put(pers, Travel(pers));
                pers.setReadyToMove(false);
            }
        });
    }

    private void movePeople() {
        movingPersons.forEach((pers, loca) -> {
            Integer XnextStep = pers.getX();
            Integer YnextStep = pers.getY();
            if ((Math.abs(pers.getX() - loca.getX()) + Math.abs(pers.getY() - loca.getY())) > 10) {
                /// Aller sur la route
                Integer counter = 0;
                while (Math.abs(pers.getX() - XnextStep) + Math.abs(pers.getY() - YnextStep) < 3 && !Objects.nonNull(city.getObjectsAtLocation(XnextStep, YnextStep)) || Objects.nonNull(city.getObjectsAtLocation(XnextStep, YnextStep)) && !city.getObjectsAtLocation(XnextStep, YnextStep).stream().anyMatch(o -> o.toString().split(" ")[0].equals("ROAD"))) {
                    YnextStep = (YnextStep / 10) * 10;
                    XnextStep = (XnextStep / 10) * 10;
                }

                //Cas ou la location est à droite
                if (XnextStep < loca.getX() && XnextStep < (loca.getX() / 10) * 10) {
                    XnextStep++;
                }
                //Cas ou la location est à gauche
                if (XnextStep > loca.getX() && XnextStep > (loca.getX() / 10) * 10 + 10) {
                    XnextStep--;
                }
                // cas location au dessus
                if (XnextStep == (loca.getX() / 10) * 10 + 10) {
                    if (loca.getY() > YnextStep) {
                        YnextStep++;
                    } else {
                        YnextStep--;
                    }
                }
                if (XnextStep == (loca.getX() / 10) * 10) {
                    if (loca.getY() > YnextStep) {
                        YnextStep++;
                    } else {
                        YnextStep--;
                    }
                }

                pers.setX(XnextStep);
                pers.setY(YnextStep);
            } else {
                Integer x = loca.getX();
                Integer y = loca.getY();
                if (loca.type == LocationTypeEnum.HOUSING) {
                    Integer i = pers.getId() - pers.getHome().getId() * 10;
                    x = pers.getHome().getX() - 2 + i;
                    y = pers.getHome().getY() - 2;
                    if (i >= 5 && i < 10) {
                        x = pers.getHome().getX() - 2 + i - 5;
                        y = pers.getHome().getY() - 1;
                    }
                    if (i >= 10 && i < 15) {
                        x = pers.getHome().getX() - 2 + i - 10;
                        y = pers.getHome().getY();
                    }
                    if (i >= 15 && i < 20) {
                        x = pers.getHome().getX() - 2 + i - 15;
                        y = pers.getHome().getY() + 1;
                    }
                    if (i >= 20 && i < 25) {
                        x = pers.getHome().getX() - 2 + i - 20;
                        y = pers.getHome().getY() + 2;
                    }
                }
                pers.setX(x);
                pers.setY(y);
                loca.addPerson(pers, (int) schedule.getSteps());
            }
        });
    }

    private void refreshMoveColorImmunityKill() {
        ArrayList<Person> toRemove = new ArrayList<>();
        ArrayList<Person> toKill = new ArrayList<>(); // dark ...
        numberOfUninfectedCitizen = numberOfCitizen - numberOfCitizenInfected - numberOfImmuneCitizen;
        this.numberOfCitizen = population.size();
        this.numberOfCitizenInfected = population.stream().filter(Person::getSick).collect(Collectors.toList()).size();
        population.forEach(pers -> {
            if (pers.getSick() && pers.getSicknessStart() < schedule.getSteps() - Virus.tickToImmunity) {
                pers.setImmune(true);
                pers.setSick(false);
                numberOfImmuneCitizen++;
            }
            if (pers.getSick() && schedule.getSteps() - pers.getSicknessStart() > Virus.tickToLethality && 2 + 10 * (pers.getAge() / 100) > new Random().nextInt(Virus.tickToLethality * Virus.chanceOfDying)) {
                if (pers.getCurrentLocation().getType() != LocationTypeEnum.HOSPITAL || 80 >= new Random().nextInt(100)) {
                    toKill.add(pers);
                    numberOfDeads++;
                } else {
                    System.out.println("Sauver par l'hopitale");
                }
            }
            if (pers.getSick()) {
                sumOfAverageR = sumOfAverageR + pers.getCoefR();
            }
            pers.getTaskHistory().removeIf(t -> schedule.getSteps() - t.getTimeStamp() > CLEAR_TASK_HISTORY);
        });
        averageR = sumOfAverageR / (float) population.stream().filter(Person::getSick).collect(Collectors.toList()).size();
        ;
        sumOfAverageR = 0;

        movingPersons.forEach((pers, loca) -> {
            city.setObjectLocation(pers, pers.getX(), pers.getY());
            if (Math.abs(pers.getX() - loca.getX()) + Math.abs(pers.getY() - loca.getY()) < 5) {
                toRemove.add(pers);
            }
        });
        toRemove.forEach(pers -> {
            movingPersons.remove(pers);
        });
        toKill.forEach(pers -> {
            movingPersons.remove(pers);
            population.remove(pers);
            pers.getCurrentLocation().getStayingTime().remove(pers);
            pers.getCurrentLocation().getPersons().remove(pers);
            city.remove(pers);
        });
    }

    public void printPop() {
        population.forEach(o -> System.out.println(o.getId()));
    }

    public void addPerson(Person person) {
        population.add(person);
    }

    public void addBuilding(Location building) {
        buildings.add(building);
    }

    public void addMoveforPerson(Person person, Location location) {
        movingPersons.put(person, location);
    }

    public ArrayList<Location> getBuildings() {
        return buildings;
    }

    public ArrayList<Person> getPopulation() {
        return population;
    }

    private Location Travel(Person person) {
        Random rand = new Random();
        Location direction = person.getHome();
        ArrayList<Location> historyBuildings = new ArrayList<>();
        person.getHistory().forEach((x) -> {
            historyBuildings.add(x.getLocation());
        });
        ArrayList<Location> filteredBuildings = buildings.stream().filter(build -> (!historyBuildings.contains(build) && build.getType() != LocationTypeEnum.HOSPITAL)).collect(Collectors.toCollection(ArrayList::new));
        Location myHospital = buildings.stream().filter(build -> build.getType() == LocationTypeEnum.HOSPITAL).findAny().get();
        if (person.getSick() && person.getAge() >= 20 && myHospital.getNumberOfOccupants() - myHospital.getCapacity() < 0 && new Random().nextInt(100) < 20 && schedule.getSteps() - person.getSicknessStart() > Virus.tickToLethality) {
            direction = myHospital;
        } else {
            if ((Math.abs(person.getX() - person.getHome().getX()) + Math.abs(person.getY() - person.getHome().getY())) < 5) {
                if (new Random().nextInt(100) < 50) {
                    if (!filteredBuildings.isEmpty()) {
                        int int_random = rand.nextInt(filteredBuildings.size());
                        direction = filteredBuildings.get(int_random);
                    }
                }
            } else {
                if (new Random().nextInt(100) > 80) {
                    if (!filteredBuildings.isEmpty()) {
                        int int_random = rand.nextInt(filteredBuildings.size());
                        direction = filteredBuildings.get(int_random);
                    }
                }
            }
        }
        return direction;

    }

    public Integer getNumberOfUninfectedCitizen() {
        return numberOfUninfectedCitizen;
    }

    public Integer getNumberOfPeopleInHospital() {
        return buildings.stream().filter(build -> build.getType() == LocationTypeEnum.HOSPITAL).findAny().get().getNumberOfOccupants();
    }

    public Integer getNumberOfImmuneCitizen() {
        return numberOfImmuneCitizen;
    }

    public Integer getNumberOfDeads() {
        return numberOfDeads;
    }

    public Integer getNumberOfCitizen() {
        return numberOfCitizen;
    }

    public Integer getNumberOfCitizenInfected() {
        return numberOfCitizenInfected;
    }

    public float getAverageR() {
        return averageR;
    }


}
