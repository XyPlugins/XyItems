package org.xyplugin.xyitems.service;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Central atomic delivery gate. Every XyItems flow that needs extra inventory slots must
 * validate here before it consumes input, runs a follow-up action, or inserts an output stack.
 */
public final class InventoryDeliveryService {
    private static final int PLAYER_STORAGE_SLOTS = 36;

    public boolean hasEmptySlots(Player player, int requiredSlots) {
        return findEmptySlots(player, requiredSlots).size() == requiredSlots;
    }

    public boolean deliver(Player player, List<ItemStack> stacks) {
        if (player == null || stacks == null || stacks.isEmpty()) return false;
        for (ItemStack stack : stacks) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() <= 0) return false;
        }
        List<Integer> slots = findEmptySlots(player, stacks.size());
        if (slots.size() != stacks.size()) return false;

        PlayerInventory inventory = player.getInventory();
        for (int index = 0; index < stacks.size(); index++) {
            inventory.setItem(slots.get(index), stacks.get(index));
        }
        return true;
    }

    public boolean deliverOne(Player player, ItemStack stack) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.add(stack);
        return deliver(player, stacks);
    }

    public List<ItemStack> split(ItemStack prototype, int amount) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        if (prototype == null || amount <= 0) return stacks;
        int maxStack = Math.max(1, prototype.getMaxStackSize());
        int remaining = amount;
        while (remaining > 0) {
            int part = Math.min(maxStack, remaining);
            ItemStack stack = prototype.clone();
            stack.setAmount(part);
            stacks.add(stack);
            remaining -= part;
        }
        return stacks;
    }

    private List<Integer> findEmptySlots(Player player, int requiredSlots) {
        List<Integer> slots = new ArrayList<Integer>();
        if (player == null || requiredSlots <= 0) return slots;
        PlayerInventory inventory = player.getInventory();
        int limit = Math.min(PLAYER_STORAGE_SLOTS, inventory.getSize());
        for (int slot = 0; slot < limit && slots.size() < requiredSlots; slot++) {
            ItemStack current = inventory.getItem(slot);
            if (current == null || current.getType() == Material.AIR || current.getAmount() <= 0) {
                slots.add(slot);
            }
        }
        return slots;
    }
}
