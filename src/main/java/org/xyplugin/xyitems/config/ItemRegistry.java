package org.xyplugin.xyitems.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Recursively loads all item definition YAML files from XyItems/items. */
public final class ItemRegistry {
    private final Map<String, ItemDefinition> definitions;
    private final Map<String, String> sources;

    private ItemRegistry(Map<String, ItemDefinition> definitions, Map<String, String> sources) {
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<String, ItemDefinition>(definitions));
        this.sources = Collections.unmodifiableMap(new LinkedHashMap<String, String>(sources));
    }

    public static ItemRegistry empty() {
        return new ItemRegistry(Collections.<String, ItemDefinition>emptyMap(), Collections.<String, String>emptyMap());
    }

    public static void ensureExampleFile(JavaPlugin plugin) {
        File example = new File(plugin.getDataFolder(), "items/Example/Example.yml");
        if (!example.exists()) plugin.saveResource("items/Example/Example.yml", false);
        File forgeExample = new File(plugin.getDataFolder(), "items/ForgeItem/ExampleForgeItem.yml");
        if (!forgeExample.exists()) plugin.saveResource("items/ForgeItem/ExampleForgeItem.yml", false);
    }

    public static LoadResult load(File directory, Logger logger) {
        if (!directory.exists() && !directory.mkdirs()) {
            return LoadResult.failure("无法创建物品配置目录: " + directory.getPath());
        }

        List<File> files = new ArrayList<File>();
        collectYamlFiles(directory, files);
        Collections.sort(files, (left, right) -> left.getPath().compareToIgnoreCase(right.getPath()));

        Map<String, ItemDefinition> definitions = new LinkedHashMap<String, ItemDefinition>();
        Map<String, String> sources = new LinkedHashMap<String, String>();
        List<String> errors = new ArrayList<String>();

        for (File file : files) {
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.load(file);
            } catch (IOException | InvalidConfigurationException exception) {
                errors.add(file.getPath() + ": YAML 无法读取: " + exception.getMessage());
                continue;
            }

            ConfigurationSection root = yaml.getConfigurationSection("items");
            if (root == null) {
                errors.add(file.getPath() + ": 缺少根节点 items:");
                continue;
            }

            for (String rawId : root.getKeys(false)) {
                ConfigurationSection section = root.getConfigurationSection(rawId);
                if (section == null) {
                    errors.add(file.getPath() + " -> " + rawId + ": 物品定义必须是一个配置节点。");
                    continue;
                }
                try {
                    ItemDefinition definition = parseItem(rawId, section);
                    String id = definition.getId();
                    if (definitions.containsKey(id)) {
                        throw new IllegalArgumentException("物品 ID 与 " + sources.get(id) + " 重复");
                    }
                    definitions.put(id, definition);
                    sources.put(id, relativePath(directory, file));
                } catch (IllegalArgumentException exception) {
                    errors.add(file.getPath() + " -> " + rawId + ": " + exception.getMessage());
                }
            }
        }

        for (String error : errors) logger.warning("[XyItems] " + error);
        if (!errors.isEmpty()) return LoadResult.failure(errors);
        return LoadResult.success(new ItemRegistry(definitions, sources));
    }

    public Optional<ItemDefinition> find(String id) {
        return id == null ? Optional.<ItemDefinition>empty()
                : Optional.ofNullable(definitions.get(normalizeId(id)));
    }

    public List<String> getIds() {
        return Collections.unmodifiableList(new ArrayList<String>(definitions.keySet()));
    }

    public int size() {
        return definitions.size();
    }

    public String getSource(String id) {
        return sources.get(normalizeId(id));
    }

    private static ItemDefinition parseItem(String rawId, ConfigurationSection section) {
        String id = normalizeId(rawId);
        if (id.isEmpty()) throw new IllegalArgumentException("物品 ID 不能为空。");

        String materialName = section.getString("material", "");
        Material material = Material.matchMaterial(materialName);
        if (material == null || material == Material.AIR) {
            throw new IllegalArgumentException("material 必须是有效的 Bukkit Material，当前为: " + materialName);
        }

        int rawData = section.getInt("data", 0);
        if (rawData < Short.MIN_VALUE || rawData > Short.MAX_VALUE) {
            throw new IllegalArgumentException("data 必须在 " + Short.MIN_VALUE + " 到 " + Short.MAX_VALUE + " 之间。");
        }

        String displayName = section.getString("display-name", "&f" + id);
        List<String> lore = new ArrayList<String>(section.getStringList("lore"));
        Map<String, QualityDefinition> qualities = parseQualities(section, displayName, lore);
        ForgeFailureDefinition forgeFailure = parseForgeFailure(section, qualities);
        return new ItemDefinition(id, material, (short) rawData, displayName, lore, qualities, forgeFailure);
    }

    private static ForgeFailureDefinition parseForgeFailure(ConfigurationSection itemSection,
                                                              Map<String, QualityDefinition> qualities) {
        ConfigurationSection forge = itemSection.getConfigurationSection("forge");
        if (forge == null) return null;
        ConfigurationSection failure = forge.getConfigurationSection("failure");
        if (failure == null) {
            throw new IllegalArgumentException("配置 forge 时必须包含 forge.failure 节点。");
        }
        if (qualities.isEmpty()) {
            throw new IllegalArgumentException("forge.failure 必须与已启用的 identify.qualities 一起使用。");
        }
        if (!failure.contains("weight")) {
            throw new IllegalArgumentException("forge.failure 必须明确配置 weight，可填写 0。");
        }
        double weight = failure.getDouble("weight", 0D);
        if (Double.isNaN(weight) || Double.isInfinite(weight) || weight < 0D) {
            throw new IllegalArgumentException("forge.failure.weight 必须大于或等于 0。");
        }
        String name = failure.getString("name", "锻造失败");
        String color = failure.getString("color", "&c");
        double totalWeight = weight;
        for (QualityDefinition quality : qualities.values()) totalWeight += quality.getWeight();
        if (Double.isNaN(totalWeight) || Double.isInfinite(totalWeight)) {
            throw new IllegalArgumentException("forge.failure与品质总权重必须是有限数值。");
        }
        return new ForgeFailureDefinition(weight, name, color);
    }

    private static Map<String, QualityDefinition> parseQualities(ConfigurationSection itemSection,
                                                                  String fallbackName, List<String> fallbackLore) {
        ConfigurationSection identify = itemSection.getConfigurationSection("identify");
        if (identify == null) return Collections.emptyMap();

        ConfigurationSection qualitySection = identify.getConfigurationSection("qualities");
        boolean enabled = identify.getBoolean("enabled", qualitySection != null);
        if (!enabled) return Collections.emptyMap();
        if (qualitySection == null || qualitySection.getKeys(false).isEmpty()) {
            throw new IllegalArgumentException("identify.enabled 为 true 时必须至少配置一个 identify.qualities 节点。");
        }

        String identifyDisplayName = identify.getString("display-name", fallbackName);
        List<String> identifyLore = identify.isList("lore")
                ? new ArrayList<String>(identify.getStringList("lore"))
                : new ArrayList<String>(fallbackLore);
        Map<String, QualityDefinition> qualities = new LinkedHashMap<String, QualityDefinition>();
        for (String rawId : qualitySection.getKeys(false)) {
            ConfigurationSection section = qualitySection.getConfigurationSection(rawId);
            if (section == null) throw new IllegalArgumentException("品质 " + rawId + " 必须是一个配置节点。");

            String id = rawId.trim();
            if (id.isEmpty()) throw new IllegalArgumentException("品质内部 ID 不能为空。");
            if (qualities.containsKey(id)) throw new IllegalArgumentException("品质内部 ID 重复: " + id);

            double weight = section.getDouble("weight", 1D);
            if (Double.isNaN(weight) || Double.isInfinite(weight) || weight <= 0D) {
                throw new IllegalArgumentException("品质 " + id + " 的 weight 必须大于 0。");
            }

            String name = section.getString("name", id);
            String color = section.getString("color", "&f");
            String displayName = section.getString("display-name", identifyDisplayName);
            List<String> lore = section.isList("lore")
                    ? new ArrayList<String>(section.getStringList("lore"))
                    : new ArrayList<String>(identifyLore);
            qualities.put(id, new QualityDefinition(id, name, color, weight, displayName, lore,
                    parseAttributes(section.getConfigurationSection("attributes"))));
        }
        double totalWeight = 0D;
        for (QualityDefinition quality : qualities.values()) totalWeight += quality.getWeight();
        if (Double.isNaN(totalWeight) || Double.isInfinite(totalWeight) || totalWeight <= 0D) {
            throw new IllegalArgumentException("identify.qualities总权重必须是有限且大于0的数值。");
        }
        return qualities;
    }

    private static Map<String, NumberRange> parseAttributes(ConfigurationSection section) {
        if (section == null) return Collections.emptyMap();
        Map<String, NumberRange> attributes = new LinkedHashMap<String, NumberRange>();
        for (String key : section.getKeys(false)) {
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("attributes 下不能存在空白属性 ID。");
            }
            Object raw = section.get(key);
            try {
                if (raw instanceof Number) {
                    double value = ((Number) raw).doubleValue();
                    attributes.put(key, new NumberRange(value, value, "0.##"));
                    continue;
                }
                ConfigurationSection range = section.getConfigurationSection(key);
                if (range == null) {
                    throw new IllegalArgumentException("必须是数值或包含 min/max 的配置节点。");
                }
                double min = range.contains("min") ? range.getDouble("min") : range.getDouble("value", 0D);
                double max = range.contains("max") ? range.getDouble("max") : min;
                String format = range.getString("format", "0.##");
                attributes.put(key, new NumberRange(min, max, format));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("属性 " + key + " 配置无效: " + exception.getMessage());
            }
        }
        return attributes;
    }

    private static String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private static void collectYamlFiles(File directory, List<File> files) {
        File[] children = directory.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) {
                collectYamlFiles(child, files);
            } else if (child.isFile() && (child.getName().endsWith(".yml") || child.getName().endsWith(".yaml"))) {
                files.add(child);
            }
        }
    }

    private static String relativePath(File root, File file) {
        String rootPath = root.getAbsolutePath();
        String path = file.getAbsolutePath();
        if (path.startsWith(rootPath)) {
            path = path.substring(rootPath.length());
            if (path.startsWith(File.separator)) path = path.substring(1);
        }
        return path.replace(File.separatorChar, '/');
    }

    public static final class LoadResult {
        private final ItemRegistry registry;
        private final List<String> errors;

        private LoadResult(ItemRegistry registry, List<String> errors) {
            this.registry = registry;
            this.errors = Collections.unmodifiableList(new ArrayList<String>(errors));
        }

        public static LoadResult success(ItemRegistry registry) {
            return new LoadResult(registry, Collections.<String>emptyList());
        }

        public static LoadResult failure(String error) {
            return failure(Collections.singletonList(error));
        }

        public static LoadResult failure(List<String> errors) {
            return new LoadResult(null, errors);
        }

        public boolean isSuccess() {
            return errors.isEmpty() && registry != null;
        }

        public ItemRegistry getRegistry() {
            return registry;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
