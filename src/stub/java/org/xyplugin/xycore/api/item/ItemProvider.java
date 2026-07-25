package org.xyplugin.xycore.api.item;

import java.util.Collection;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

/** Compile-only XyCore item provider API stub. */
public interface ItemProvider {
    String getId();

    boolean isAvailable();

    Collection<String> getItemIds();

    Optional<ItemStack> createItem(String itemId, int amount);

    default Optional<String> identify(ItemStack item) {
        return Optional.empty();
    }
}
