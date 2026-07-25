package org.xyplugin.xyitems.listener;

import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xyitems.XyItemsPlugin;
import org.xyplugin.xyitems.config.ItemDefinition;
import org.xyplugin.xyitems.item.ItemFactory;

/** Listens only for main-hand right clicks and exits after one NBT lookup for unrelated items. */
public final class ItemIdentifyListener implements Listener {
    private final XyItemsPlugin plugin;

    public ItemIdentifyListener(XyItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack held = event.getItem();
        if (held == null || held.getType() == Material.AIR || held.getAmount() <= 0) return;

        Optional<String> id = plugin.getItemFactory().getItemId(held);
        if (!id.isPresent()) return;
        Optional<ItemDefinition> definition = plugin.getRegistry().find(id.get());
        if (!definition.isPresent() || !plugin.getItemFactory().isUnidentified(held, definition.get())) return;

        // The interaction belongs to XyItems from this point onward, including a rejected full-inventory attempt.
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!player.hasPermission("xyitems.use")) {
            plugin.send(player, plugin.message("no-permission"));
            return;
        }

        // This is intentionally a global XyItems output precondition, including a one-item hand stack.
        // Future crafting, forging, enhancement, and exchange flows use the same inventory delivery service.
        if (!plugin.getDelivery().hasEmptySlots(player, 1)) {
            plugin.send(player, plugin.message("inventory-full"));
            return;
        }

        Optional<ItemFactory.IdentificationResult> identified = plugin.getItemFactory().identify(held, definition.get());
        if (!identified.isPresent()) return;

        ItemStack result = identified.get().getItem();
        int amount = held.getAmount();
        if (amount == 1) {
            player.getInventory().setItemInMainHand(result);
        } else {
            // Delivery occurs before the source stack is reduced. A failed delivery leaves the source untouched.
            if (!plugin.getDelivery().deliverOne(player, result)) {
                plugin.send(player, plugin.message("inventory-full"));
                return;
            }
            ItemStack remaining = held.clone();
            remaining.setAmount(amount - 1);
            player.getInventory().setItemInMainHand(remaining);
        }

        player.updateInventory();
        plugin.send(player, plugin.formatMessage("identified", "{item}", identified.get().getQualityName()));
    }
}
