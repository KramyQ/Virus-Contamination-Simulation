package com.utc.models;

import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

import static com.utc.constants.AppConstants.MINUTES_PER_TICK;
import static com.utc.models.Virus.transmissibility;

public class Location extends GridObject implements Steppable {
    private final Schedule schedule;
    private final HashMap<Person, Integer> persons;
    private final HashMap<Person, Integer> stayingTime;
    public LocationTypeEnum type;
    private Integer id;
    private Boolean maskRequired;
    private Boolean openAir;
    private Integer capacity;
    private Integer maxStayTime;
    private Integer numberOfOccupants;
    private Integer numberOfOccupantsInfected;
    private Occupancy OccupancyLabel;


    public Location(Integer x, Integer y, Integer id, Boolean maskRequired, Boolean openAir, Integer capacity, Integer maxStayTime, LocationTypeEnum type, Schedule schedule, Occupancy OccupancyLabel) {
        super(x, y);
        this.id = id;
        this.maskRequired = maskRequired;
        this.openAir = openAir;
        this.capacity = capacity;
        this.maxStayTime = maxStayTime;
        this.type = type;
        this.persons = new HashMap<>();
        this.stayingTime = new HashMap<>();
        this.schedule = schedule;
        this.OccupancyLabel = OccupancyLabel;
        this.numberOfOccupants = 0;
        this.numberOfOccupantsInfected = 0;
    }

    @Override
    public void step(SimState simState) {
        checkArrivals();
        infectOccupants();
        whoNeedToLeave();
        updateLabel();
    }

    void checkArrivals() {
        numberOfOccupants = persons.keySet().stream().collect(Collectors.toList()).size();
        numberOfOccupantsInfected = persons.keySet().stream().filter(Person::getSick).collect(Collectors.toList()).size();
        ArrayList<Person> currentPersons = new ArrayList<>();
        ArrayList<Person> filteredPersons = new ArrayList<>();
        persons.forEach((x, y) -> currentPersons.add(x));
        stayingTime.forEach((x, y) -> filteredPersons.add(x));

        ArrayList<Person> arrivingPersons = currentPersons.stream().filter(pers -> (!filteredPersons.contains(pers))).collect(Collectors.toCollection(ArrayList::new));
        arrivingPersons.forEach(pers -> {
            pers.setCurrentLocation(this);
            pers.getTaskHistory().add(new TaskHistory(this, (int) schedule.getSteps()));
            stayingTime.put(pers, this.maxStayTime + this.maxStayTime * (new Random().nextInt(40) - 20) / 100
            );
        });
    }

    public void updateLabel() {
        OccupancyLabel.setNumberOfOccupants(numberOfOccupants);
    }

    private void whoNeedToLeave() {
        ArrayList<Person> personsToKick = new ArrayList<>();
        stayingTime.forEach((pers, tickNumber) -> {
            if (tickNumber <= schedule.getSteps() - persons.get(pers) - 100 || pers.getCurrentLocation().getType() == LocationTypeEnum.HOSPITAL && pers.isImmune()) {
                personsToKick.add(pers);
            }
        });
        personsToKick.forEach(pers -> {
            pers.setReadyToMove(true);
            if (this.type.equals(LocationTypeEnum.HOUSING)) {
                pers.setX(pers.getHome().getX());
                pers.setY(pers.getHome().getY());
            }
            persons.remove(pers);
            stayingTime.remove(pers);
        });
    }

    private void infectOccupants() {
        HashMap<Person, Integer> newPersons = new HashMap<>(persons);
        Integer infectedPeople = newPersons.keySet().stream().filter(Person::getSick).collect(Collectors.toList()).size();
        newPersons.forEach((person, integer) -> {
            if (!person.isImmune() && !person.getSick()) {
                BigDecimal firstImpact = BigDecimal.valueOf((schedule.getSteps() - integer) * MINUTES_PER_TICK).multiply(getPromiscuity()).multiply(BigDecimal.valueOf(infectedPeople)).multiply(BigDecimal.valueOf(.2));
                BigDecimal maskImpact = firstImpact.multiply(BigDecimal.valueOf((maskRequired ? 1 : 0) * 0.90));
                BigDecimal openAirImpact = firstImpact.multiply(BigDecimal.valueOf((openAir ? 1 : 0) * 0.2));
                BigDecimal result = firstImpact.subtract(maskImpact).subtract(openAirImpact);
                newPersons.replace(person, result.intValue());

                if (result.floatValue() > transmissibility * MINUTES_PER_TICK) {
                    float step1 = transmissibility * MINUTES_PER_TICK / result.floatValue();
                    float step2 = 1 - step1;
                    float step3 = step2 / 3F;
                    float infectChance = step3 * 100;
                    MersenneTwisterFast rnd = new MersenneTwisterFast();
                    int probaCeiling = type == LocationTypeEnum.HOUSING ? 10000 : 100;
                    if (rnd.nextInt(probaCeiling) < infectChance) {
                        newPersons.keySet().stream().filter(Person::getSick).forEach(pers -> pers.setCoefR(pers.getCoefR() + 1 / infectedPeople.floatValue()));
                        person.setSick(true);
                        person.setSicknessStart((int) schedule.getSteps());

                    }
                }
            }
        });
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getPromiscuity() {
        return BigDecimal.valueOf(persons.size()).divide(BigDecimal.valueOf(capacity), 3, RoundingMode.HALF_UP);
    }

    public LocationTypeEnum getType() {
        return type;
    }

    public Boolean getMaskRequired() {
        return maskRequired;
    }

    public void setMaskRequired(Boolean maskRequired) {
        this.maskRequired = maskRequired;
    }

    public HashMap<Person, Integer> getPersons() {
        return persons;
    }

    public HashMap<Person, Integer> getStayingTime() {
        return stayingTime;
    }

    public Boolean getOpenAir() {
        return openAir;
    }

    public void setOpenAir(Boolean openAir) {
        this.openAir = openAir;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getMaxStayTime() {
        return maxStayTime;
    }

    public void setMaxStayTime(Integer maxStayTime) {
        this.maxStayTime = maxStayTime;
    }

    public Integer getNumberOfOccupants() {
        return numberOfOccupants;
    }

    public Integer getNumberOfOccupantsInfected() {
        return numberOfOccupantsInfected;
    }

    public void addPerson(Person person, Integer timeStamp) {
        this.persons.put(person, timeStamp);
    }

    public void setOccupancyLabel(Occupancy occupancyLabel) {
        OccupancyLabel = occupancyLabel;
    }

    @Override
    public String toString() {
        return this.type + " " + id;
    }
}
