package com.example.appengine.quarkus.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An aspect of the energy consumed by a house")
public class EnergyFeature {

    @Schema(description = "The type of the energy feature")
    public EnergyFeatureType type;

    @Schema(description = "The expenses of the feature in NOK/Month")
    public Double expense;

    @Schema(description = "The energy used in KW / month")
    public Double energy;

    @Schema(description = "The pollution caused in yearly CO2 equivalents")
    public Double pollution;
}
