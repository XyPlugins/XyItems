package org.xyplugin.xyitems.config;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One weighted identification result. The id is intentionally arbitrary YAML text. */
public final class QualityDefinition {
    private final String id;
    private final String name;
    private final String color;
    private final double weight;
    private final String displayName;
    private final List<String> lore;
    private final Map<String, NumberRange> attributes;

    public QualityDefinition(String id, String name, String color, double weight, String displayName,
                             List<String> lore, Map<String, NumberRange> attributes) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.weight = weight;
        this.displayName = displayName;
        this.lore = Collections.unmodifiableList(new ArrayList<String>(lore));
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<String, NumberRange>(attributes));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public double getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public Map<String, String> rollAttributes() {
        return rollAttributesWithStrength().getValues();
    }

    /** Rolls each attribute independently and keeps its raw range position for strength. */
    public RolledAttributes rollAttributesWithStrength() {
        Map<String, String> values = new LinkedHashMap<String, String>();
        Map<String, Double> strengths = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, NumberRange> entry : attributes.entrySet()) {
            NumberRange range = entry.getValue();
            double raw = range.rollValue();
            values.put(entry.getKey(), range.format(raw));
            strengths.put(entry.getKey(), range.strengthPercent(raw));
        }
        return new RolledAttributes(values, strengths);
    }

    public Map<String, NumberRange> getAttributes() {
        return attributes;
    }

    public static final class RolledAttributes {
        private final Map<String, String> values;
        private final Map<String, Double> strengths;

        private RolledAttributes(Map<String, String> values, Map<String, Double> strengths) {
            this.values = Collections.unmodifiableMap(new LinkedHashMap<String, String>(values));
            this.strengths = Collections.unmodifiableMap(new LinkedHashMap<String, Double>(strengths));
        }

        public Map<String, String> getValues() {
            return values;
        }

        public Map<String, Double> getStrengths() {
            return strengths;
        }
    }
}
