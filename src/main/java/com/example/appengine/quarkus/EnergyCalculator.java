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
        } else {
            calculator.analyseApartment(house, analysis);
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

    private Double energyByAreaAndYearHeuristic(Double area, Integer constructionYear) {
        if (area != null && constructionYear != null) {
            return energyByAreaHeuristic(area, false) * energyByConstructionYearHeuristic(constructionYear);
        } else if (area != null) {
            return energyByAreaHeuristic(area, false);
        } else {
            return null;
        }
    }

    private double solarCellBenefit(double houseArea) {
        var daysInYear = 365.24;
        var inverseSizeScale = 1 / 60.0;
        var wattsPerPanel = 100.0;
        return houseArea * daysInYear * inverseSizeScale * wattsPerPanel;
    }

    private double geoBenefit() {
        var daysInYear = 365.24;
        var wattBenefit = 1000;

        return wattBenefit * daysInYear;
    }

    private double heatExchangeFactor() {
        return 0.5;
    }

    private double districtHeatingFactor() {
        return 0.5;
    }

    void analyzeStandaloneHouse(House house, EnergyAnalysis analysis) {

        var weights = house.area != null && house.floors != null ? weights(house.area, house.floors) : null;
        Double energy = energyByAreaAndYearHeuristic(house.area, house.constructionYear);

        var energyUsage = energy;

        if (energyUsage != null && house.improvements != null && house.improvements.contains(Improvement.SOLAR_CELLS)) {
            var solarCellBenefit = solarCellBenefit(house.area);
            energyUsage = max(0.0, energyUsage - solarCellBenefit);
        }

        if (energyUsage != null && house.improvements != null && house.improvements.contains(Improvement.GEOTHERMAL)) {
            var geoBenefit = geoBenefit();
            energyUsage = max(0.0, energyUsage - geoBenefit);
        }

        if (energyUsage != null && house.improvements != null && house.improvements.contains(Improvement.HEAT_EXCHANGE_UNIT)) {
            energyUsage *= heatExchangeFactor();
        }

        if (energyUsage != null && house.improvements != null && house.improvements.contains(Improvement.DISTRICT_HEATING)) {
            energyUsage *= districtHeatingFactor();
        }

        analysis.features.addAll(listOf(
                weights != null && energy != null ? CEILINGS.createFeature(energy * weights.floor, pricePerKwH, CO2KiloPerKwH) : null,
                weights != null && energy != null ? WALLS.createFeature(energy * weights.wall, pricePerKwH, CO2KiloPerKwH) : null,
                weights != null && energy != null ? FLOORS.createFeature(energy * weights.floor, pricePerKwH, CO2KiloPerKwH) : null,
                weights != null && energy != null ? WINDOWS.createFeature(energy * weights.window, pricePerKwH, CO2KiloPerKwH) : null,
                energyUsage != null ? HEATING_UNIT.createFeature(energyUsage, pricePerKwH, CO2KiloPerKwH) : null
        ));
    }

    void analyseApartment(House house, EnergyAnalysis analysis) {

        var weights = house.area != null && house.floors != null ? weights(house.area, house.floors) : null;
        Double energy = energyByAreaAndYearHeuristic(house.area, house.constructionYear);

        if (weights != null && energy != null) {
            // Fudge energy for apartments so they don't leak to above and below
            energy *= (weights.wall + weights.window) / (weights.window + weights.floor + weights.wall + weights.window);
        }

        analysis.features.addAll(listOf(
                weights != null && energy != null ? WALLS.createFeature(energy * weights.wall, pricePerKwH, CO2KiloPerKwH) : null,
                weights != null && energy != null ? WINDOWS.createFeature(energy * weights.window, pricePerKwH, CO2KiloPerKwH) : null,
                energy != null ? HEATING_UNIT.createFeature(energy, pricePerKwH, CO2KiloPerKwH) : null
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
