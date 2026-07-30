package org.xyplugin.xyitems.config;

/** Immutable failure entry participating in a forged item's final weighted result. */
public final class ForgeFailureDefinition {
    private final double weight;
    private final String name;
    private final String color;

    public ForgeFailureDefinition(double weight, String name, String color) {
        this.weight = weight;
        this.name = name;
        this.color = color;
    }

    public double getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
