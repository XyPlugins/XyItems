package org.xyplugin.xycore.api.item;

import java.util.Optional;
import org.bukkit.inventory.ItemStack;

/** Compile-only XyCore NBT tag API stub. */
public interface ItemTagService {
    boolean isAvailable();

    Optional<String> getString(ItemStack item, String key);

    ItemStack setString(ItemStack item, String key, String value);

    ItemStack remove(ItemStack item, String key);
}
