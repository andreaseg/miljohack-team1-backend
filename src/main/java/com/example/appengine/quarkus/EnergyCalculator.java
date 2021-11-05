package com.example.appengine.quarkus;

import com.example.appengine.quarkus.model.EnergyAnalysis;
import com.example.appengine.quarkus.model.EnergyFeature;
import com.example.appengine.quarkus.model.House;
import com.example.appengine.quarkus.model.Improvement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.appengine.quarkus.model.EnergyFeatureType.*;
import static com.example.appengine.quarkus.util.Math.lerp;
import static java.lang.Math.*;

public class EnergyCalculator {

    private final Double pricePerKwH;
    private final Double CO2KiloPerKwH;

    public EnergyCalculator(Double pricePerKwH, Double CO2KiloPerKwH) {
        this.pricePerKwH = pricePerKwH;
        this.CO2KiloPerKwH = CO2KiloPerKwH;
    }

    public static EnergyAnalysis analyze(House house, Double pricePerKwH, Double CO2KiloPerKwH) {

        var calculator = new EnergyCalculator(pricePerKwH, CO2KiloPerKwH);

        var analysis = new EnergyAnalysis();
        analysis.features = new ArrayList<>();

        if (house.floors == null) {
            house.floors = 1;
        }

        if (house.isApartment == null) {
            house.isApartment = false;
        }

        if (!house.isApartment) {
            calculator.analyzeStandaloneHouse(house, analysis);
        }

        calculator.analyseHouse(house, analysis);

        return analysis;
    }

    private List<EnergyFeature> listOf(EnergyFeature... items) {
        return Stream.of(items).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private double energyByConstructionYearHeuristic(int year) {
        var startYear = 1993.0;
        var startKwh = 18000.0;
        var endYear = 2015.0;
        var endKwh = 16000.0;

        return lerp(startKwh / (endKwh), 1.0, (year - startYear) / (endYear - startYear));
    }

    private double energyByAreaHeuristic(double area, boolean isApartment) {
        var typeFactor = isApartment ? 0.5 : 1.0;

        var bigUse = 40000.0;
        var bigSize = 300.0;
        var smallUse = 16000.0;
        var smallSize = 120.0;

        var interpolation = (area - smallSize) / (bigSize - smallSize);

        return typeFactor * lerp(smallUse, bigUse, interpolation);
    }

    private static class Weights {
        Double roof;
        Double floor;
        Double window;
        Double wall;
    }

    private Weights weights(double area, int floors) {
        var footprint = area / floors;
        var wallLength = sqrt(footprint);
        var wallHeight = floors * 2.6;
        var weightedFloorSize = 2 * footprint;
        var weightedRoofSize = 2 * footprint;
        var weightedWallSize = 0.3 * 4 * wallHeight * wallLength;
        var weightedWindowSize = 0.7 * 4 * wallHeight * wallLength;
        var surface = weightedFloorSize + weightedRoofSize + weightedWallSize + weightedWindowSize;

        var weights = new Weights();
        weights.roof = weightedRoofSize / surface;
        weights.floor = weightedFloorSize / surface;
        weights.window = weightedWindowSize / surface;
        weights.wall = weightedWallSize / surface;


        if (abs(weights.roof + weights.floor + weights.window + weights.wall - 1) > 0.0001) {
            throw new IllegalStateException("Invalid weights");
        }

        return weights;
    }

    void analyzeStandaloneHouse(House house, EnergyAnalysis analysis) {

        var weights = house.area != null && house.floors != null ? weights(house.area, house.floors) : null;
        Double energy;
        if (house.area != null && house.constructionYear != null) {
            energy = energyByAreaHeuristic(house.area, false) * energyByConstructionYearHeuristic(house.constructionYear);
        } else if (house.area != null) {
            energy = energyByAreaHeuristic(house.area, false);
        } else {
            energy = null;
        }

        var energyUsage = energy;

        if (energyUsage != null && house.improvements != null && house.improvements.contains(Improvement.SOLAR_CELLS)) {
            var solarCellBenefit = house.area != null ? house.area / 60 * 100.0 : 100.0;
            solarCellBenefit *= 365.24;
            energyUsage = max(0.0, energyUsage - solarCellBenefit);
        }

        if (energyUsage != null && house.improvements != null && house.improvements.contains(Improvement.GEOTHERMAL)) {
            var geoBenefit = 1000 * 365;
            energyUsage = max(0.0, energyUsage - geoBenefit);
        }

        if (energyUsage != null && house.improvements != null && house.improvements.contains(Improvement.HEAT_EXCHANGE_UNIT)) {
            energyUsage *= 0.5;
        }

        if (energyUsage != null && house.improvements != null && house.improvements.contains(Improvement.DISTRICT_HEATING)) {
            energyUsage *= 0.5;
        }

        analysis.features.addAll(listOf(
                weights != null && energy != null ? CEILINGS.createFeature(energy * weights.floor, pricePerKwH, CO2KiloPerKwH) : null,
                weights != null && energy != null ? WALLS.createFeature(energy * weights.wall, pricePerKwH, CO2KiloPerKwH) : null,
                weights != null && energy != null ? FLOORS.createFeature(energy * weights.floor, pricePerKwH, CO2KiloPerKwH) : null,
                weights != null && energy != null ? WINDOWS.createFeature(energy * weights.window, pricePerKwH, CO2KiloPerKwH) : null,
                energyUsage != null ? HEATING_UNIT.createFeature(energyUsage, pricePerKwH, CO2KiloPerKwH) : null
        ));
    }

    void analyseHouse(House house, EnergyAnalysis analysis) {

        var showerEnergy = house.improvements != null && house.improvements.contains(Improvement.SHOWER) ? 1100.0 : 2370.0;

        analysis.features.addAll(listOf(
                FRIDGE.createFeature(470.0, pricePerKwH, CO2KiloPerKwH),
                WASHING_MACHINE.createFeature(520.0, pricePerKwH, CO2KiloPerKwH),
                CLOTHES_DRYER.createFeature(470.0, pricePerKwH, CO2KiloPerKwH),
                SHOWER.createFeature(showerEnergy, pricePerKwH, CO2KiloPerKwH)
        ));
    }
}
