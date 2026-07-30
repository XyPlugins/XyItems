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
        Map<String, String> values = new LinkedHashMap<String, String>();
        for (Map.Entry<String, NumberRange> entry : attributes.entrySet()) {
            values.put(entry.getKey(), entry.getValue().roll());
        }
        return values;
    }
}
