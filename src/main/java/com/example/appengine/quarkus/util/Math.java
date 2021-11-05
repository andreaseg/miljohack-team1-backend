package com.example.appengine.quarkus.util;

public class Math {
    private Math() {}

    public static double lerp(double min, double max, double interpolation) {
        return interpolation * max + (1 - interpolation) * min;
    }
}
