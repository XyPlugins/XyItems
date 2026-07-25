package org.xyplugin.xycore.api.item;

import java.util.Collection;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

/** Compile-only XyCore item library API stub. */
public interface ItemLibraryService {
    void registerProvider(ItemProvider provider);

    void unregisterProvider(String providerId);

    Optional<ItemProvider> getProvider(String providerId);

    Collection<ItemProvider> getProviders();

    Optional<ItemStack> create(String namespacedId, int amount);

    Collection<String> getItemIds(String providerId);
}
