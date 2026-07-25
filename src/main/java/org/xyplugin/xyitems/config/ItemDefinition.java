package org.xyplugin.xyitems.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xyplugin.xyitems.util.Text;

/** Immutable item definition assembled from one YAML item section. */
public final class ItemDefinition {
    private final String id;
    private final Material material;
    private final short data;
    private final String displayName;
    private final List<String> lore;
    private final Map<String, QualityDefinition> qualities;

    public ItemDefinition(String id, Material material, short data, String displayName, List<String> lore,
                          Map<String, QualityDefinition> qualities) {
        this.id = id;
        this.material = material;
        this.data = data;
        this.displayName = displayName;
        this.lore = Collections.unmodifiableList(new ArrayList<String>(lore));
        this.qualities = Collections.unmodifiableMap(new LinkedHashMap<String, QualityDefinition>(qualities));
    }

    public String getId() {
        return id;
    }

    public boolean isIdentifiable() {
        return !qualities.isEmpty();
    }

    public Map<String, QualityDefinition> getQualities() {
        return qualities;
    }

    public ItemStack createUnidentified(int amount) {
        return createItem(displayName, lore, Collections.<String, String>emptyMap(), amount);
    }

    public IdentifiedResult createIdentified(int amount) {
        QualityDefinition quality = chooseQuality();
        if (quality == null) return null;

        Map<String, String> rolledAttributes = quality.rollAttributes();
        Map<String, String> placeholders = new LinkedHashMap<String, String>();
        placeholders.put("品质.名称", quality.getName());
        placeholders.put("品质.颜色", quality.getColor());
        placeholders.put("quality.name", quality.getName());
        placeholders.put("quality.color", quality.getColor());
        placeholders.putAll(rolledAttributes);

        ItemStack item = createItem(quality.getDisplayName(), quality.getLore(), placeholders, amount);
        return new IdentifiedResult(item, quality, rolledAttributes);
    }

    private ItemStack createItem(String nameTemplate, List<String> loreTemplates,
                                 Map<String, String> placeholders, int amount) {
        ItemStack item = new ItemStack(material, Math.max(1, amount));
        item.setDurability(data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(Text.replacePlaceholders(nameTemplate, placeholders)));
            List<String> renderedLore = new ArrayList<String>();
            for (String line : loreTemplates) {
                renderedLore.add(Text.color(Text.replacePlaceholders(line, placeholders)));
            }
            meta.setLore(renderedLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private QualityDefinition chooseQuality() {
        double total = 0D;
        for (QualityDefinition quality : qualities.values()) total += quality.getWeight();
        if (total <= 0D) return null;

        double selected = ThreadLocalRandom.current().nextDouble(total);
        double cursor = 0D;
        QualityDefinition fallback = null;
        for (QualityDefinition quality : qualities.values()) {
            cursor += quality.getWeight();
            fallback = quality;
            if (selected < cursor) return quality;
        }
        return fallback;
    }

    public static final class IdentifiedResult {
        private final ItemStack item;
        private final QualityDefinition quality;
        private final Map<String, String> attributes;

        private IdentifiedResult(ItemStack item, QualityDefinition quality, Map<String, String> attributes) {
            this.item = item;
            this.quality = quality;
            this.attributes = Collections.unmodifiableMap(new LinkedHashMap<String, String>(attributes));
        }

        public ItemStack getItem() {
            return item;
        }

        public QualityDefinition getQuality() {
            return quality;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }
    }
}
