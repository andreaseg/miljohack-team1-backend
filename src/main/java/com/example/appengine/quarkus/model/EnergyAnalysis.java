package com.example.appengine.quarkus.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Analysis result after analyzing the energy characteristics of a house")
public class EnergyAnalysis {

    @Schema(description = "A list of features derived for the house the analysis is performed on")
    public List<EnergyFeature> features;

}
