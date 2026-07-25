package org.xyplugin.xyitems.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;

/** Small text helpers shared by configuration rendering and player messages. */
public final class Text {
    private Text() {
    }

    public static String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value == null ? "" : value);
    }

    public static List<String> color(List<String> values) {
        List<String> result = new ArrayList<String>();
        if (values == null) return result;
        for (String value : values) result.add(color(value));
        return result;
    }

    /** Supports the readable forms &lt;key&gt;, %key%, and &lt;%key%&gt;. */
    public static String replacePlaceholders(String template, Map<String, String> values) {
        String result = template == null ? "" : template;
        if (values == null) return result;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isEmpty()) continue;
            String value = entry.getValue() == null ? "" : entry.getValue();
            // Replace the nested syntax first: replacing %key% first would turn <%key%> into <value>.
            result = result.replace("<%" + key + "%>", value);
            result = result.replace("<" + key + ">", value);
            result = result.replace("%" + key + "%", value);
        }
        return result;
    }
}
