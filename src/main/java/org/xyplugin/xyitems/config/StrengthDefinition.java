package org.xyplugin.xyitems.config;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Controls the optional strength display for one identified item.
 * Strength is calculated inside the selected quality, not across qualities.
 */
public final class StrengthDefinition {
    private static final StrengthDefinition DISABLED = new StrengthDefinition(false, 10,
            "&cl", "&7l", "0.0", Collections.<String, Double>emptyMap());

    private final boolean enabled;
    private final int barLength;
    private final String filled;
    private final String empty;
    private final String percentFormat;
    private final Map<String, Double> weights;

    public StrengthDefinition(boolean enabled, int barLength, String filled, String empty,
                              String percentFormat, Map<String, Double> weights) {
        this.enabled = enabled;
        this.barLength = barLength;
        this.filled = filled == null ? "" : filled;
        this.empty = empty == null ? "" : empty;
        this.percentFormat = percentFormat == null || percentFormat.trim().isEmpty()
                ? "0.0" : percentFormat;
        // Validate the format at load time instead of during a player interaction.
        formatter();
        this.weights = Collections.unmodifiableMap(new LinkedHashMap<String, Double>(weights));
    }

    public static StrengthDefinition disabled() {
        return DISABLED;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getWeight(String attribute) {
        Double configured = weights.get(attribute);
        // An empty weights map means every random attribute participates equally.
        return configured == null ? 1D : configured.doubleValue();
    }

    public String formatPercent(double percent) {
        return formatter().format(Math.max(0D, Math.min(100D, percent)));
    }

    public String renderBar(double percent) {
        if (!enabled || barLength <= 0) return "";
        double bounded = Math.max(0D, Math.min(100D, percent));
        // 只要强度大于0就显示一格，避免低强度物品看起来像完全没有强度。
        int filledCount = bounded <= 0D ? 0 : (int) Math.ceil(bounded * barLength / 100D);
        // 连续段只输出一次颜色码，避免生成 &cl&cl&cl... 这种冗余 Lore。
        return renderSegment(filled, filledCount) + renderSegment(empty, barLength - filledCount);
    }

    private String renderSegment(String token, int count) {
        if (count <= 0 || token == null || token.isEmpty()) return "";
        int colorLength = leadingColorCodeLength(token);
        String color = token.substring(0, colorLength);
        String body = token.substring(colorLength);
        // 只写颜色时使用默认条码字符 l，例如 filled: '&c'。
        if (body.isEmpty()) body = "l";
        if (body.length() == 1) {
            StringBuilder result = new StringBuilder(color.length() + count);
            result.append(color);
            for (int index = 0; index < count; index++) result.append(body);
            return result.toString();
        }

        StringBuilder result = new StringBuilder(token.length() * count);
        for (int index = 0; index < count; index++) result.append(token);
        return result.toString();
    }

    private int leadingColorCodeLength(String token) {
        int offset = 0;
        while (offset + 1 < token.length()
                && (token.charAt(offset) == '&' || token.charAt(offset) == '\u00a7')
                && isColorCode(token.charAt(offset + 1))) {
            offset += 2;
        }
        return offset;
    }

    private boolean isColorCode(char value) {
        char lower = Character.toLowerCase(value);
        return (lower >= '0' && lower <= '9') || (lower >= 'a' && lower <= 'f')
                || lower == 'k' || lower == 'l' || lower == 'm' || lower == 'n'
                || lower == 'o' || lower == 'r' || lower == 'x';
    }

    private DecimalFormat formatter() {
        DecimalFormat result = new DecimalFormat(percentFormat,
                DecimalFormatSymbols.getInstance(Locale.US));
        result.setGroupingUsed(false);
        return result;
    }
}
