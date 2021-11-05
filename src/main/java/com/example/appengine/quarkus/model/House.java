package com.example.appengine.quarkus.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Object representing a house")
public class House {

    @Schema(description = "The area of the house in square meters")
    public Double area;

    @Schema(description = "The number of floors in the house. If not set may default to 1")
    public Integer floors;

    @Schema(description = "The year the house was constructed")
    public Integer constructionYear;

    @Schema(description = "The energy grade of the house")
    public String energyGrade;

    @Schema(description = "The municipality number of the address for the house")
    public String municipalityNumber;

    @Schema(description = "Flag to be set if the house is to be treated as an apartment, changing which suggestions may be given")
    public Boolean isApartment;

    @Schema(description = "List of improvements made to the house")
    public List<Improvement> improvements;

    @Schema(hidden = true)
    public String data;


}
