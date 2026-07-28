package org.xyplugin.xyitems.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.xyplugin.xyitems.XyItemsPlugin;
import org.xyplugin.xyitems.config.ItemDefinition;

/** /xyitems command and completion handler. */
public final class XyItemsCommand implements CommandExecutor, TabCompleter {
    private static final int LIST_PAGE_SIZE = 8;
    private static final int MAX_COMMAND_AMOUNT = 2304;
    private final XyItemsPlugin plugin;

    public XyItemsCommand(XyItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if ("list".equals(subcommand)) return list(sender, args);
        if ("info".equals(subcommand)) return info(sender, args);
        if ("get".equals(subcommand)) return get(sender, args);
        if ("give".equals(subcommand)) return give(sender, args);
        if ("reload".equals(subcommand)) return reload(sender);
        sendHelp(sender);
        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        if (!requirePermission(sender, "xyitems.list")) return true;
        List<String> ids = plugin.getRegistry().getIds();
        if (ids.isEmpty()) {
            plugin.send(sender, plugin.message("no-items"));
            return true;
        }

        int page = 1;
        if (args.length > 1) {
            try {
                page = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }
        int totalPages = (ids.size() + LIST_PAGE_SIZE - 1) / LIST_PAGE_SIZE;
        page = Math.min(page, totalPages);
        int from = (page - 1) * LIST_PAGE_SIZE;
        int to = Math.min(from + LIST_PAGE_SIZE, ids.size());
        plugin.send(sender, "&bXyItems &7(" + page + "/" + totalPages + "): &f"
                + join(ids.subList(from, to)));
        return true;
    }

    private boolean info(CommandSender sender, String[] args) {
        if (!requirePermission(sender, "xyitems.list")) return true;
        if (args.length < 2) {
            plugin.send(sender, "&c用法: /xyitems info <物品ID>");
            return true;
        }
        Optional<ItemDefinition> definition = plugin.getRegistry().find(args[1]);
        if (!definition.isPresent()) {
            plugin.send(sender, plugin.formatMessage("item-not-found", "{item}", args[1]));
            return true;
        }
        ItemDefinition item = definition.get();
        plugin.send(sender, "&b物品 ID: &f" + item.getId());
        plugin.send(sender, "&b鉴定: &f" + (item.isIdentifiable() ? "开启" : "关闭"));
        plugin.send(sender, "&b配置文件: &f" + plugin.getRegistry().getSource(item.getId()));
        return true;
    }

    private boolean give(CommandSender sender, String[] args) {
        if (!requirePermission(sender, "xyitems.give")) return true;
        if (args.length < 3) {
            plugin.send(sender, "&c用法: /xyitems give <玩家> <物品ID> [数量]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            plugin.send(sender, plugin.formatMessage("player-not-found", "{player}", args[1]));
            return true;
        }
        return deliverItem(sender, target, args[2], args.length > 3 ? args[3] : null, true);
    }

    private boolean get(CommandSender sender, String[] args) {
        if (!requirePermission(sender, "xyitems.get")) return true;
        if (!(sender instanceof Player)) {
            plugin.send(sender, plugin.message("players-only"));
            return true;
        }
        if (args.length < 2) {
            plugin.send(sender, "&c用法: /xyitem get <物品ID> [数量]");
            return true;
        }
        return deliverItem(sender, (Player) sender, args[1], args.length > 2 ? args[2] : null, false);
    }

    private boolean deliverItem(CommandSender sender, Player target, String itemId,
                                String amountText, boolean administrativeGive) {
        Optional<ItemDefinition> definition = plugin.getRegistry().find(itemId);
        if (!definition.isPresent()) {
            plugin.send(sender, plugin.formatMessage("item-not-found", "{item}", itemId));
            return true;
        }

        int amount = 1;
        if (amountText != null) {
            try {
                amount = Integer.parseInt(amountText);
            } catch (NumberFormatException ignored) {
                amount = 0;
            }
        }
        if (amount <= 0 || amount > MAX_COMMAND_AMOUNT) {
            plugin.send(sender, plugin.message("invalid-amount"));
            return true;
        }

        Optional<ItemStack> prototype = plugin.getItemFactory().createBase(definition.get(), 1);
        if (!prototype.isPresent()) {
            plugin.send(sender, "&c无法生成该物品，请检查 XyCore NBT 服务。" );
            return true;
        }
        List<ItemStack> stacks = plugin.getDelivery().split(prototype.get(), amount);
        if (!plugin.getDelivery().deliver(target, stacks)) {
            plugin.send(target, plugin.message("inventory-full"));
            if (sender != target) plugin.send(sender, plugin.message("inventory-full"));
            return true;
        }

        target.updateInventory();
        if (administrativeGive) {
            plugin.send(sender, plugin.formatMessage("item-given", "{player}", target.getName(), "{amount}",
                    String.valueOf(amount), "{item}", definition.get().getId()));
        }
        if (!administrativeGive || sender != target) {
            plugin.send(target, plugin.formatMessage("item-received", "{amount}", String.valueOf(amount),
                    "{item}", definition.get().getId()));
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!requirePermission(sender, "xyitems.reload")) return true;
        if (!plugin.reloadItemDefinitions()) {
            plugin.send(sender, plugin.message("reload-failed"));
            return true;
        }
        plugin.send(sender, plugin.formatMessage("reloaded", "{count}", String.valueOf(plugin.getRegistry().size())));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        boolean includesGet = false;
        for (String line : plugin.getConfig().getStringList("messages.help")) {
            String normalized = line.toLowerCase(Locale.ROOT);
            if (normalized.contains("/xyitem get") || normalized.contains("/xyitems get")
                    || normalized.contains("/xyi get")) {
                includesGet = true;
            }
            plugin.send(sender, line);
        }
        // Existing 1.0 installations keep their customized config.yml, so add only the missing help line at runtime.
        if (!includesGet) plugin.send(sender, "&e/xyitem get <物品ID> [数量] &7获得物品");
    }

    private boolean requirePermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        plugin.send(sender, plugin.message("no-permission"));
        return false;
    }

    private String join(List<String> values) {
        StringBuilder joined = new StringBuilder();
        for (String value : values) {
            if (joined.length() > 0) joined.append(", ");
            joined.append(value);
        }
        return joined.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return complete(args[0], Arrays.asList("get", "give", "list", "info", "reload", "help"));
        }
        if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
            List<String> names = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
            return complete(args[1], names);
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            return complete(args[2], plugin.getRegistry().getIds());
        }
        if (args.length == 2 && "get".equalsIgnoreCase(args[0])) {
            return complete(args[1], plugin.getRegistry().getIds());
        }
        if (args.length == 3 && "get".equalsIgnoreCase(args[0])) {
            return complete(args[2], Arrays.asList("1", "16", "32", "64"));
        }
        if (args.length == 2 && "info".equalsIgnoreCase(args[0])) {
            return complete(args[1], plugin.getRegistry().getIds());
        }
        return Collections.emptyList();
    }

    private List<String> complete(String input, List<String> candidates) {
        List<String> result = new ArrayList<String>();
        StringUtil.copyPartialMatches(input, candidates, result);
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}
