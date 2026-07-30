package org.xyplugin.xyitems.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xyitems.XyItemsPlugin;
import org.xyplugin.xyitems.config.ItemDefinition;

public final class XyItemsApiImpl implements XyItemsApi {
    private final XyItemsPlugin plugin;

    public XyItemsApiImpl(XyItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<ItemStack> createItem(String itemId, int amount) {
        Optional<ItemDefinition> definition = plugin.getRegistry().find(itemId);
        return definition.isPresent() ? plugin.getItemFactory().createBase(definition.get(), amount) : Optional.empty();
    }

    @Override
    public Optional<ItemStack> createIdentifiedItem(String itemId, String qualityId, int amount) {
        Optional<ItemDefinition> definition = plugin.getRegistry().find(itemId);
        return definition.isPresent()
                ? plugin.getItemFactory().createIdentified(definition.get(), qualityId, amount)
                : Optional.<ItemStack>empty();
    }

    @Override
    public Optional<ForgeOutcomeProfile> getForgeOutcomeProfile(String itemId) {
        Optional<ItemDefinition> definition = plugin.getRegistry().find(itemId);
        return definition.isPresent() ? definition.get().createForgeOutcomeProfile() : Optional.empty();
    }

    @Override
    public ForgeRollResult rollForgeOutcome(String itemId) {
        Optional<ItemDefinition> definition = plugin.getRegistry().find(itemId);
        if (!definition.isPresent()) return ForgeRollResult.unavailable(itemId);

        Optional<ForgeOutcomeProfile> profile = definition.get().createForgeOutcomeProfile();
        if (!profile.isPresent() || profile.get().getTotalWeight() <= 0D) {
            return ForgeRollResult.unavailable(itemId);
        }

        double selected = ThreadLocalRandom.current().nextDouble(profile.get().getTotalWeight());
        double cursor = 0D;
        ForgeOutcomeProfile.Outcome selectedOutcome = null;
        for (ForgeOutcomeProfile.Outcome outcome : profile.get().getOutcomes()) {
            cursor += outcome.getWeight();
            selectedOutcome = outcome;
            if (selected < cursor) break;
        }
        if (selectedOutcome == null) return ForgeRollResult.unavailable(itemId);
        if (selectedOutcome.isFailure()) {
            return ForgeRollResult.failure(definition.get().getId(), selectedOutcome.getName(),
                    selectedOutcome.getColor());
        }

        Optional<ItemStack> built = plugin.getItemFactory().createIdentified(
                definition.get(), selectedOutcome.getId(), 1);
        return built.isPresent()
                ? ForgeRollResult.success(definition.get().getId(), selectedOutcome.getId(),
                selectedOutcome.getName(), selectedOutcome.getColor(), built.get())
                : ForgeRollResult.unavailable(itemId);
    }

    @Override
    public Optional<String> getItemId(ItemStack item) {
        return plugin.getItemFactory().getItemId(item);
    }

    @Override
    public Optional<String> getQualityId(ItemStack item) {
        return plugin.getItemFactory().getQualityId(item);
    }

    @Override
    public Map<String, String> getRolledAttributes(ItemStack item) {
        return plugin.getItemFactory().getRolledAttributes(item);
    }

    @Override
    public boolean isXyItem(ItemStack item) {
        return getItemId(item).isPresent();
    }

    @Override
    public boolean isUnidentified(ItemStack item) {
        Optional<String> id = getItemId(item);
        if (!id.isPresent()) return false;
        Optional<ItemDefinition> definition = plugin.getRegistry().find(id.get());
        return definition.isPresent() && plugin.getItemFactory().isUnidentified(item, definition.get());
    }

    @Override
    public boolean hasDeliverySpace(Player player, int requiredSlots) {
        return plugin.getDelivery().hasEmptySlots(player, requiredSlots);
    }

    @Override
    public boolean deliverItems(Player player, List<ItemStack> output) {
        return plugin.getDelivery().deliver(player, output);
    }
}
