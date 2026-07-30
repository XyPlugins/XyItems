package org.xyplugin.xyitems.api;

import java.util.Optional;
import org.bukkit.inventory.ItemStack;

/** One final forge roll. A successful roll already carries its exact identified item. */
public final class ForgeRollResult {
    public enum Status {
        SUCCESS,
        FAILURE,
        UNAVAILABLE
    }

    private final Status status;
    private final String itemId;
    private final String outcomeId;
    private final String outcomeName;
    private final String outcomeColor;
    private final ItemStack item;

    private ForgeRollResult(Status status, String itemId, String outcomeId, String outcomeName,
                            String outcomeColor, ItemStack item) {
        this.status = status;
        this.itemId = itemId;
        this.outcomeId = outcomeId;
        this.outcomeName = outcomeName;
        this.outcomeColor = outcomeColor;
        this.item = item == null ? null : item.clone();
    }

    public static ForgeRollResult success(String itemId, String qualityId, String qualityName,
                                          String qualityColor, ItemStack item) {
        return new ForgeRollResult(Status.SUCCESS, itemId, qualityId, qualityName, qualityColor, item);
    }

    public static ForgeRollResult failure(String itemId, String name, String color) {
        return new ForgeRollResult(Status.FAILURE, itemId, "failure", name, color, null);
    }

    public static ForgeRollResult unavailable(String itemId) {
        return new ForgeRollResult(Status.UNAVAILABLE, itemId, "", "", "", null);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isFailure() {
        return status == Status.FAILURE;
    }

    public String getItemId() {
        return itemId;
    }

    /** Returns "failure" for a failed roll, or the selected quality id for success. */
    public String getOutcomeId() {
        return outcomeId;
    }

    public String getOutcomeName() {
        return outcomeName;
    }

    public String getOutcomeColor() {
        return outcomeColor;
    }

    /** Returns a defensive copy of the already identified successful result. */
    public Optional<ItemStack> getItem() {
        return item == null ? Optional.<ItemStack>empty() : Optional.of(item.clone());
    }
}
