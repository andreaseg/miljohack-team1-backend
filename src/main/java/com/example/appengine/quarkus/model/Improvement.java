package com.example.appengine.quarkus.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An improvement installed in a house or apartment")
public enum Improvement {
    HEAT_EXCHANGE_UNIT,
    WALL_ISOLATION,
    TARGETED_ISOLATION,
    WINDOWS,
    SOLAR_CELLS,
    GEOTHERMAL,
    DISTRICT_HEATING
}
