package org.xyplugin.xyitems.item;

import java.util.Collection;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xycore.api.item.ItemProvider;
import org.xyplugin.xyitems.XyItemsPlugin;
import org.xyplugin.xyitems.config.ItemDefinition;

/** Makes configured XyItems definitions available to XyCore's unified item library. */
public final class XyItemsProvider implements ItemProvider {
    private final XyItemsPlugin plugin;
    private final String providerId;

    public XyItemsProvider(XyItemsPlugin plugin, String providerId) {
        this.plugin = plugin;
        this.providerId = providerId;
    }

    @Override
    public String getId() {
        return providerId;
    }

    @Override
    public boolean isAvailable() {
        return plugin.isEnabled();
    }

    @Override
    public Collection<String> getItemIds() {
        return plugin.getRegistry().getIds();
    }

    @Override
    public Optional<ItemStack> createItem(String itemId, int amount) {
        Optional<ItemDefinition> definition = plugin.getRegistry().find(itemId);
        return definition.isPresent() ? plugin.getItemFactory().createBase(definition.get(), amount) : Optional.empty();
    }

    @Override
    public Optional<String> identify(ItemStack item) {
        return plugin.getItemFactory().getItemId(item);
    }
}
