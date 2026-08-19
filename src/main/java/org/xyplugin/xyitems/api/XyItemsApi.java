package org.xyplugin.xyitems.api;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Public API for future XY forging, enhancement, exchange, and storage plugins. */
public interface XyItemsApi {
    Optional<ItemStack> createItem(String itemId, int amount);

    /** Creates an identified item at an explicitly selected quality without another quality roll. */
    Optional<ItemStack> createIdentifiedItem(String itemId, String qualityId, int amount);

    /** Returns the final failure/quality probability snapshot configured for forging this item. */
    Optional<ForgeOutcomeProfile> getForgeOutcomeProfile(String itemId);

    /** Performs exactly one final roll and embeds the identified result when successful. */
    ForgeRollResult rollForgeOutcome(String itemId);

    Optional<String> getItemId(ItemStack item);

    Optional<String> getQualityId(ItemStack item);

    /** Exact attribute rolls persisted by XyItems during identification. */
    Map<String, String> getRolledAttributes(ItemStack item);

    /** Strength percentage persisted during identification, when the item enables strength display. */
    Optional<Double> getStrengthPercent(ItemStack item);

    boolean isXyItem(ItemStack item);

    boolean isUnidentified(ItemStack item);

    /** Checks the strict XyItems delivery rule: required output slots must be empty first. */
    boolean hasDeliverySpace(Player player, int requiredSlots);

    /** Atomically inserts output stacks into empty main-inventory slots, or inserts none. */
    boolean deliverItems(Player player, List<ItemStack> output);
}
