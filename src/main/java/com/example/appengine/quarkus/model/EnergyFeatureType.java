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
    ENERGY_SAVER_SHOWER
}
