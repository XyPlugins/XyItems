package org.xyplugin.xyitems;

import java.io.File;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.xyplugin.xyitems.api.XyItemsApi;
import org.xyplugin.xyitems.api.XyItemsApiImpl;
import org.xyplugin.xyitems.command.XyItemsCommand;
import org.xyplugin.xyitems.config.ItemRegistry;
import org.xyplugin.xyitems.integration.XyCoreBridge;
import org.xyplugin.xyitems.item.ItemFactory;
import org.xyplugin.xyitems.item.XyItemsProvider;
import org.xyplugin.xyitems.listener.ItemIdentifyListener;
import org.xyplugin.xyitems.service.InventoryDeliveryService;
import org.xyplugin.xyitems.util.Text;

/** XyItems entry point for Paper/Spigot 1.12.2. */
public final class XyItemsPlugin extends JavaPlugin {
    private static final String DEFAULT_LOCAL_PREFIX = "&7[&bXyItems&7]&r ";
    private static XyItemsPlugin instance;

    private XyCoreBridge core;
    private ItemRegistry registry = ItemRegistry.empty();
    private ItemFactory itemFactory;
    private InventoryDeliveryService delivery;
    private XyItemsProvider provider;
    private String providerId;
    private XyItemsApi api;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        core = new XyCoreBridge(this);
        if (!core.connect()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        itemFactory = new ItemFactory(core);
        delivery = new InventoryDeliveryService();
        api = new XyItemsApiImpl(this);
        ItemRegistry.ensureExampleFile(this);
        if (!loadDefinitions(false)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        refreshProvider();
        Bukkit.getPluginManager().registerEvents(new ItemIdentifyListener(this), this);
        XyItemsCommand command = new XyItemsCommand(this);
        if (getCommand("xyitems") != null) {
            getCommand("xyitems").setExecutor(command);
            getCommand("xyitems").setTabCompleter(command);
        }
        getLogger().info("XyItems " + getDescription().getVersion() + " enabled with " + registry.size()
                + " item definitions. Provider: " + providerId + ".");
    }

    @Override
    public void onDisable() {
        if (core != null && provider != null) core.unregisterProvider(provider.getId());
        provider = null;
        instance = null;
    }

    public boolean reloadItemDefinitions() {
        reloadConfig();
        ItemRegistry.ensureExampleFile(this);
        if (!loadDefinitions(true)) return false;
        refreshProvider();
        return true;
    }

    private boolean loadDefinitions(boolean keepExistingOnFailure) {
        File directory = new File(getDataFolder(), "items");
        ItemRegistry.LoadResult result = ItemRegistry.load(directory, getLogger());
        if (!result.isSuccess()) {
            if (!keepExistingOnFailure) {
                getLogger().severe("XyItems 启动失败：物品配置存在错误。");
            }
            return false;
        }
        registry = result.getRegistry();
        return true;
    }

    private void refreshProvider() {
        String rawProviderId = getConfig().getString("settings.provider-id", "xyitems");
        String configured = (rawProviderId == null ? "xyitems" : rawProviderId).trim().toLowerCase(Locale.ROOT);
        if (configured.isEmpty()) configured = "xyitems";
        if (provider != null && configured.equals(provider.getId())) return;
        if (provider != null) core.unregisterProvider(provider.getId());
        providerId = configured;
        provider = new XyItemsProvider(this, providerId);
        core.registerProvider(provider);
    }

    /** 玩家玩法提示：有 XyCore 时使用 XyCore 统一前缀。 */
    public void sendPlayer(Player player, String message) {
        if (player == null || message == null || message.trim().isEmpty()) return;
        core.send(player, message);
    }

    /** 管理/排错提示：保留 XyItems 自身前缀。 */
    public void sendLocal(CommandSender sender, String message) {
        if (sender == null || message == null || message.trim().isEmpty()) return;
        sender.sendMessage(Text.color(localPrefix() + message));
    }

    /** 默认用于命令反馈，按最终约定走插件自身前缀。 */
    public void send(CommandSender sender, String message) {
        sendLocal(sender, message);
    }

    private String localPrefix() {
        return getConfig().getString("messages.prefix", DEFAULT_LOCAL_PREFIX);
    }

    public String message(String key) {
        return getConfig().getString("messages." + key, "");
    }

    public String formatMessage(String key, String... replacements) {
        String result = message(key);
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            result = result.replace(replacements[index], replacements[index + 1]);
        }
        return result;
    }

    public static XyItemsPlugin getInstance() {
        return instance;
    }

    public XyItemsApi getApi() {
        return api;
    }

    public ItemRegistry getRegistry() {
        return registry;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    public InventoryDeliveryService getDelivery() {
        return delivery;
    }
}
