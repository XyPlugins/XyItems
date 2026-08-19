package org.xyplugin.xyitems.config;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/** A configured random numeric range rendered with a stable decimal format. */
public final class NumberRange {
    private final double min;
    private final double max;
    private final String format;

    public NumberRange(double min, double max, String format) {
        if (Double.isNaN(min) || Double.isNaN(max) || Double.isInfinite(min) || Double.isInfinite(max)) {
            throw new IllegalArgumentException("range values must be finite");
        }
        if (min > max) throw new IllegalArgumentException("min cannot be greater than max");
        this.min = min;
        this.max = max;
        this.format = format == null || format.trim().isEmpty() ? "0.##" : format;
        // Validate at load time so a typo cannot fail an item interaction later.
        formatter();
    }

    public String roll() {
        return format(rollValue());
    }

    /** Rolls the raw value before applying the configured display format. */
    public double rollValue() {
        double value;
        if (min == max) {
            value = min;
        } else if ("0".equals(format) && min == Math.rint(min) && max == Math.rint(max)
                && min >= Long.MIN_VALUE && max < Long.MAX_VALUE) {
            value = ThreadLocalRandom.current().nextLong((long) min, (long) max + 1L);
        } else {
            value = ThreadLocalRandom.current().nextDouble(min, Math.nextUp(max));
        }
        return value;
    }

    /** Formats a value with the same stable format used by {@link #roll()}. */
    public String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.00000001D && "0.##".equals(format)) {
            return String.valueOf((long) Math.rint(value));
        }
        return formatter().format(value);
    }

    /** Returns the value's relative position in this range as a percentage. */
    public double strengthPercent(double value) {
        if (max <= min) return 100D;
        return Math.max(0D, Math.min(100D, (value - min) * 100D / (max - min)));
    }

    private DecimalFormat formatter() {
        DecimalFormat decimalFormat = new DecimalFormat(format, DecimalFormatSymbols.getInstance(Locale.US));
        decimalFormat.setGroupingUsed(false);
        return decimalFormat;
    }
}
