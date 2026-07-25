package org.xyplugin.xyitems.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.xyplugin.xycore.api.XyCore;
import org.xyplugin.xycore.api.XyCoreApi;
import org.xyplugin.xycore.api.item.ItemProvider;
import org.xyplugin.xycore.api.item.ItemTagService;
import org.xyplugin.xyitems.util.Text;

/** Required XyCore integration: NBT tags, item provider registration, and common prefix. */
public final class XyCoreBridge {
    private final JavaPlugin plugin;
    private JavaPlugin corePlugin;
    private XyCoreApi api;

    public XyCoreBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        Plugin candidate = Bukkit.getPluginManager().getPlugin("XyCore");
        if (!(candidate instanceof JavaPlugin) || !candidate.isEnabled()) {
            plugin.getLogger().severe("XyItems requires an enabled XyCore installation.");
            return false;
        }
        try {
            api = XyCore.get();
            corePlugin = (JavaPlugin) candidate;
            if (api.getItemTags() == null || !api.getItemTags().isAvailable()) {
                plugin.getLogger().severe("XyCore NBT item tags are unavailable. XyItems cannot safely identify items.");
                return false;
            }
            plugin.getLogger().info("Connected to XyCore " + api.getVersion() + ".");
            return true;
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Could not access XyCore API: " + exception.getMessage());
            return false;
        }
    }

    public ItemTagService getItemTags() {
        if (api == null) throw new IllegalStateException("XyCore bridge is not connected");
        return api.getItemTags();
    }

    public void registerProvider(ItemProvider provider) {
        api.getItems().registerProvider(provider);
    }

    public void unregisterProvider(String providerId) {
        if (api != null) api.getItems().unregisterProvider(providerId);
    }

    public void send(Player player, String body) {
        if (player == null || body == null || body.trim().isEmpty()) return;
        player.sendMessage(Text.color(getPrefix() + body));
    }

    public String getPrefix() {
        if (corePlugin == null) return "&7[&bXyCore&7]&r";
        return corePlugin.getConfig().getString("messages.prefix", "&7[&bXyCore&7]&r");
    }
}
