package org.xyplugin.xyitems.api;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Public API for future XY forging, enhancement, exchange, and storage plugins. */
public interface XyItemsApi {
    Optional<ItemStack> createItem(String itemId, int amount);

    Optional<String> getItemId(ItemStack item);

    Optional<String> getQualityId(ItemStack item);

    /** Exact attribute rolls persisted by XyItems during identification. */
    Map<String, String> getRolledAttributes(ItemStack item);

    boolean isXyItem(ItemStack item);

    boolean isUnidentified(ItemStack item);

    /** Checks the strict XyItems delivery rule: required output slots must be empty first. */
    boolean hasDeliverySpace(Player player, int requiredSlots);

    /** Atomically inserts output stacks into empty main-inventory slots, or inserts none. */
    boolean deliverItems(Player player, List<ItemStack> output);
}
