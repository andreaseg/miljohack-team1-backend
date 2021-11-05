package com.example.appengine.quarkus.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The type of energy feature")
public enum EnergyFeatureType {
    WALLS,
    WINDOWS,
    FLOORS,
    CEILINGS,
    HEATING_UNIT,
    FRIDGE,
    WASHING_MACHINE,
    CLOTHES_DRYER,
    SHOWER;

    private static final Double HOURS_IN_A_YEAR = 365.24 * 24;

    public EnergyFeature createFeature(Double kwh, Double pricePerKwH, Double CO2KiloPerKwH) {
        var feature = new EnergyFeature();
        feature.type = this;
        feature.energy = kwh;

        feature.expense = pricePerKwH * kwh;

        feature.pollution = CO2KiloPerKwH * kwh;

        return feature;
    }
}
