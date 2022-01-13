package com.utc;

import com.utc.models.*;
import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

import static com.utc.constants.AppConstants.GRID_SIZE;


public class Simulation extends SimState {

    static Integer experienceScale = 2;
    public static SparseGrid2D city = new SparseGrid2D(GRID_SIZE * experienceScale, GRID_SIZE * experienceScale);
    public Behaviour behaviourHandler;
    boolean maskMandatory = true;

    public Simulation(long seed) {
        super(seed);
        System.out.println("Seed : " + seed);
    }

    public static Integer getTickToLethality() {
        return Virus.tickToLethality;
    }

    public static void setTickToLethality(Integer tickToLethality) {
        Virus.tickToLethality = tickToLethality;
    }

    public static Integer getTickToImmunity() {
        return Virus.tickToImmunity;
    }

    public static void setTickToImmunity(Integer tickToImmunity) {
        Virus.tickToImmunity = tickToImmunity;
    }

    public static Integer getChanceOfDying() {
        return Virus.chanceOfDying;
    }

    public static void setChanceOfDying(Integer chanceOfDying) {
        Virus.chanceOfDying = chanceOfDying;
    }

    public static float getTransmissibility() {
        return Virus.transmissibility;
    }

    public static void setTransmissibility(float transmissibility) {
        Virus.transmissibility = transmissibility;
    }

    public Integer getExperienceScale() {
        return experienceScale;
    }

    public static void setExperienceScale(Integer experienceScale) {
        Simulation.experienceScale = experienceScale;
    }
    //Function<Integer, Int2D> locationFromPlace = y -> new Int2D( 5, y);

    public Behaviour getBehaviourHandler() {
        return behaviourHandler;
    }

    public void start() {
        super.start();
        city.clear();
        behaviourHandler = new Behaviour(city, super.schedule);
        initLocations(behaviourHandler);
        initFamilies(behaviourHandler);
        initRoads();
        schedule.scheduleRepeating(behaviourHandler);
    }

    private void initFamilies(Behaviour BehaviourHandler) {
        for (int i = 0; i < ((GRID_SIZE * experienceScale / 10 * GRID_SIZE * experienceScale / 10)) - experienceScale * 7 * 3 - 1; i++) {
            House house = getNewRandomHouse();
            List<Person> person = getNewRandomFamily(house, BehaviourHandler);
            city.setObjectLocation(house, house.getX(), house.getY());
            person.forEach(p -> city.setObjectLocation(p, p.getX(), p.getY()));
        }
    }

    private List<Person> getNewRandomFamily(House house, Behaviour BehaviourHandler) {
        Integer familySize = 5 + new Random().nextInt(20);
        List<Person> family = new ArrayList<>();
        for (int i = 0; i < familySize; i++) {
            // Integer age, Integer severity, Boolean comorbidities, Location home, Location jobPlace
            Integer x = house.getX() - 2 + i;
            Integer y = house.getY() - 2;
            if (i >= 5 && i < 10) {
                x = house.getX() - 2 + i - 5;
                y = house.getY() - 1;
            }
            if (i >= 10 && i < 15) {
                x = house.getX() - 2 + i - 10;
                y = house.getY();
            }
            if (i >= 15 && i < 20) {
                x = house.getX() - 2 + i - 15;
                y = house.getY() + 1;
            }
            if (i >= 20 && i < 25) {
                x = house.getX() - 2 + i - 20;
                y = house.getY() + 2;
            }
            if (new Random().nextInt(100) >= 98) {
                Person newPerson = new Person(x, y, i + house.getId() * 10, 15 + new Random().nextInt(74), 0, new Random().nextBoolean(), house, house, true);
                BehaviourHandler.addPerson(newPerson);
                family.add(newPerson);
            } else {
                Person newPerson = new Person(x, y, i + house.getId() * 10, 15 + new Random().nextInt(74), 0, new Random().nextBoolean(), house, null, null);
                BehaviourHandler.addPerson(newPerson);
                family.add(newPerson);
            }
        }

        return family;
    }

    private House getNewRandomHouse() {
        Integer nextId = (int) city.allObjects.stream().filter(o -> o.getClass().equals(House.class)).count() + 1;
        Int2D location = getFreeLocation(false);
        House newHouse = new House(location.x, location.y, nextId, true, false, 100, 60, LocationTypeEnum.HOUSING, super.schedule, null);
        schedule.scheduleRepeating(newHouse);
        return newHouse;
    }

    private Person getNewRandomPerson() {
        return new Person(1, 2);
    }

    void initLocations(Behaviour BehaviourHandler) {
        Occupancy chartHospital = new Occupancy(0, 500);
        Location hospital = new Location(5, 5, 1, maskMandatory, false, 80, 400, LocationTypeEnum.HOSPITAL, super.schedule, chartHospital);
        city.setObjectLocation(hospital, 5, 5);
        BehaviourHandler.addBuilding(hospital);
        schedule.scheduleRepeating(hospital);
        IntStream.rangeClosed(1, experienceScale * 7).forEach(i -> {
            Occupancy chartCatering = new Occupancy(0, 10);
            Occupancy chartEssential = new Occupancy(0, 10);
            Occupancy chartEntertainement = new Occupancy(0, 10);
            Int2D cateringLocation = getFreeLocation(true);
            Int2D EssentialLocation = getFreeLocation(true);
            Int2D EntertainementLocation = getFreeLocation(true);
            Location catering = new Location(cateringLocation.x, cateringLocation.y, i, maskMandatory, false, 160 * experienceScale, 40, LocationTypeEnum.CATERING, super.schedule, chartCatering);
            Location essentials = new Location(EssentialLocation.x, EssentialLocation.y, i, maskMandatory, false, 160 * experienceScale, 40, LocationTypeEnum.ESSENTIALS, super.schedule, chartEssential);
            Location entertainements = new Location(EntertainementLocation.x, EntertainementLocation.y, i, maskMandatory, false, 160 * experienceScale, 40, LocationTypeEnum.ENTERTAINMENT, super.schedule, chartEntertainement);
            city.setObjectLocation(catering, catering.getX(), catering.getY());
            city.setObjectLocation(essentials, essentials.getX(), essentials.getY());
            city.setObjectLocation(entertainements, entertainements.getX(), entertainements.getY());
            city.setObjectLocation(chartCatering, catering.getX() - 2, catering.getY() - 3);
            city.setObjectLocation(chartEssential, essentials.getX() - 2, essentials.getY() - 3);
            city.setObjectLocation(chartEntertainement, entertainements.getX() - 2, entertainements.getY() - 3);

            BehaviourHandler.addBuilding(catering);
            schedule.scheduleRepeating(catering);
            BehaviourHandler.addBuilding(essentials);
            schedule.scheduleRepeating(essentials);
            BehaviourHandler.addBuilding(entertainements);
            schedule.scheduleRepeating(entertainements);
        });
    }

    private void initRoads() {
        for (int i = 0; i <= GRID_SIZE * experienceScale; i++) {
            for (int j = 0; j <= GRID_SIZE * experienceScale / 10; j++) {
                city.setObjectLocation(new Road(i * 10 + j), i, j * 10);
            }
        }
        for (int i = 0; i <= GRID_SIZE * experienceScale / 10; i++) {
            for (int j = 0; j <= GRID_SIZE * experienceScale; j++) {
                city.setObjectLocation(new Road(i * 10 + j), i * 10, j);
            }
        }
    }

    private Int2D getFreeLocation(boolean random) {
        Integer x = 0;
        Integer y = 0;
        if (random == false) {
            for (int i = 0; i < GRID_SIZE * experienceScale / 10; i++) {
                for (int j = 0; j < GRID_SIZE * experienceScale / 10; j++) {
                    if (!Objects.nonNull(city.getObjectsAtLocation(5 + i * 10, 5 + j * 10))) {
                        return new Int2D(5 + i * 10, 5 + j * 10);
                    }
                }
            }
        } else {
            for (int i = new Random().nextInt(GRID_SIZE * experienceScale / 10); i < GRID_SIZE * experienceScale / 10; i++) {
                for (int j = new Random().nextInt(GRID_SIZE * experienceScale / 10); j < GRID_SIZE * experienceScale / 10; j++) {
                    if (!Objects.nonNull(city.getObjectsAtLocation(5 + i * 10, 5 + j * 10))) {
                        return new Int2D(5 + i * 10, 5 + j * 10);
                    }
                }
            }
        }
        return new Int2D(x, y);
    }

    public Integer getNumberOfCitizens() {
        return behaviourHandler.getNumberOfCitizen();
    }

    public Integer getNumberOfUninfectedCitizen() {
        return behaviourHandler.getNumberOfUninfectedCitizen();
    }

    public Integer getNumberOfInfectedCitizen() {
        return behaviourHandler.getNumberOfCitizenInfected();
    }

    public Integer getNumberOfImmuneCitizen() {
        return behaviourHandler.getNumberOfImmuneCitizen();
    }

    public Integer getNumberOfDeads() {
        return behaviourHandler.getNumberOfDeads();
    }

    public Integer getNumberOfPeopleInHospital() {
        return behaviourHandler.getNumberOfPeopleInHospital();
    }

    public boolean isMaskMandatory() {
        return maskMandatory;
    }

    public void setMaskMandatory(boolean maskMandatory) {
        this.maskMandatory = maskMandatory;
        behaviourHandler.getBuildings().forEach(build -> build.setMaskRequired(maskMandatory));
    }
}

