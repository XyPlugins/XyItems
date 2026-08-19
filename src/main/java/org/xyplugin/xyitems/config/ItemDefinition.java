package org.xyplugin.xyitems.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xyplugin.xyitems.api.ForgeOutcomeProfile;
import org.xyplugin.xyitems.util.Text;

/** Immutable item definition assembled from one YAML item section. */
public final class ItemDefinition {
    private final String id;
    private final Material material;
    private final short data;
    private final String displayName;
    private final List<String> lore;
    private final boolean unbreakable;
    private final boolean hideUnbreakable;
    private final Map<String, QualityDefinition> qualities;
    private final ForgeFailureDefinition forgeFailure;
    private final StrengthDefinition strength;
    private final String identifyActionName;

    public ItemDefinition(String id, Material material, short data, String displayName, List<String> lore,
                          Map<String, QualityDefinition> qualities, ForgeFailureDefinition forgeFailure) {
        this(id, material, data, displayName, lore, false, true, qualities, forgeFailure);
    }

    public ItemDefinition(String id, Material material, short data, String displayName, List<String> lore,
                          boolean unbreakable, boolean hideUnbreakable,
                          Map<String, QualityDefinition> qualities, ForgeFailureDefinition forgeFailure) {
        this(id, material, data, displayName, lore, unbreakable, hideUnbreakable, qualities,
                forgeFailure, StrengthDefinition.disabled(), "鉴定");
    }

    public ItemDefinition(String id, Material material, short data, String displayName, List<String> lore,
                          boolean unbreakable, boolean hideUnbreakable,
                          Map<String, QualityDefinition> qualities, ForgeFailureDefinition forgeFailure,
                          StrengthDefinition strength, String identifyActionName) {
        this.id = id;
        this.material = material;
        this.data = data;
        this.displayName = displayName;
        this.lore = Collections.unmodifiableList(new ArrayList<String>(lore));
        this.unbreakable = unbreakable;
        this.hideUnbreakable = hideUnbreakable;
        this.qualities = Collections.unmodifiableMap(new LinkedHashMap<String, QualityDefinition>(qualities));
        this.forgeFailure = forgeFailure;
        this.strength = strength == null ? StrengthDefinition.disabled() : strength;
        this.identifyActionName = identifyActionName == null || identifyActionName.trim().isEmpty()
                ? "鉴定" : identifyActionName;
    }

    public String getId() {
        return id;
    }

    public boolean isIdentifiable() {
        return !qualities.isEmpty();
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public boolean shouldHideUnbreakable() {
        return hideUnbreakable;
    }

    public StrengthDefinition getStrength() {
        return strength;
    }

    public String getIdentifyActionName() {
        return identifyActionName;
    }

    public Map<String, QualityDefinition> getQualities() {
        return qualities;
    }

    public Optional<ForgeOutcomeProfile> createForgeOutcomeProfile() {
        if (forgeFailure == null || qualities.isEmpty()) return Optional.empty();
        double total = forgeFailure.getWeight();
        for (QualityDefinition quality : qualities.values()) total += quality.getWeight();
        if (Double.isNaN(total) || Double.isInfinite(total) || total <= 0D) return Optional.empty();

        List<ForgeOutcomeProfile.Outcome> outcomes = new ArrayList<ForgeOutcomeProfile.Outcome>();
        outcomes.add(new ForgeOutcomeProfile.Outcome(ForgeOutcomeProfile.Outcome.Type.FAILURE, "failure",
                forgeFailure.getName(), forgeFailure.getColor(), forgeFailure.getWeight(),
                forgeFailure.getWeight() * 100D / total));
        for (QualityDefinition quality : qualities.values()) {
            outcomes.add(new ForgeOutcomeProfile.Outcome(ForgeOutcomeProfile.Outcome.Type.QUALITY,
                    quality.getId(), quality.getName(), quality.getColor(), quality.getWeight(),
                    quality.getWeight() * 100D / total));
        }
        return Optional.of(new ForgeOutcomeProfile(id, outcomes));
    }

    public ItemStack createUnidentified(int amount) {
        return createItem(displayName, lore, actionPlaceholders(), amount);
    }

    public IdentifiedResult createIdentified(int amount) {
        QualityDefinition quality = chooseQuality();
        return quality == null ? null : createIdentified(quality, amount);
    }

    /** Builds an exact quality and deliberately performs no weighted quality selection. */
    public IdentifiedResult createIdentified(String qualityId, int amount) {
        if (qualityId == null) return null;
        QualityDefinition quality = qualities.get(qualityId.trim());
        return quality == null ? null : createIdentified(quality, amount);
    }

    private IdentifiedResult createIdentified(QualityDefinition quality, int amount) {

        QualityDefinition.RolledAttributes rolled = quality.rollAttributesWithStrength();
        Map<String, String> rolledAttributes = rolled.getValues();
        Map<String, String> placeholders = new LinkedHashMap<String, String>();
        placeholders.put("品质.名称", quality.getName());
        placeholders.put("品质.颜色", quality.getColor());
        placeholders.put("quality.name", quality.getName());
        placeholders.put("quality.color", quality.getColor());
        placeholders.putAll(actionPlaceholders());
        placeholders.putAll(rolledAttributes);

        double strengthPercent = calculateStrength(rolled.getStrengths());
        String strengthText = strength.isEnabled() ? strength.formatPercent(strengthPercent) : "";
        String strengthBar = strength.isEnabled() ? strength.renderBar(strengthPercent) : "";
        placeholders.put("strength.percent", strengthText);
        placeholders.put("strength.bar", strengthBar);
        placeholders.put("强度.百分比", strengthText);
        placeholders.put("强度.条", strengthBar);

        ItemStack item = createItem(quality.getDisplayName(), quality.getLore(), placeholders, amount);
        return new IdentifiedResult(item, quality, rolledAttributes, strengthPercent, strengthBar);
    }

    private Map<String, String> actionPlaceholders() {
        Map<String, String> placeholders = new LinkedHashMap<String, String>();
        placeholders.put("identify.action", identifyActionName);
        placeholders.put("动作.名称", identifyActionName);
        return placeholders;
    }

    private double calculateStrength(Map<String, Double> attributeStrengths) {
        if (!strength.isEnabled() || attributeStrengths.isEmpty()) return 0D;
        double weighted = 0D;
        double total = 0D;
        for (Map.Entry<String, Double> entry : attributeStrengths.entrySet()) {
            double weight = strength.getWeight(entry.getKey());
            if (weight <= 0D || Double.isNaN(weight) || Double.isInfinite(weight)) continue;
            weighted += entry.getValue() * weight;
            total += weight;
        }
        return total <= 0D ? 0D : Math.max(0D, Math.min(100D, weighted / total));
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
            meta.setUnbreakable(unbreakable);
            if (unbreakable && hideUnbreakable) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }
        return item;
    }

    private QualityDefinition chooseQuality() {
        double total = 0D;
        for (QualityDefinition quality : qualities.values()) total += quality.getWeight();
        if (Double.isNaN(total) || Double.isInfinite(total) || total <= 0D) return null;

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
        private final double strengthPercent;
        private final String strengthBar;

        private IdentifiedResult(ItemStack item, QualityDefinition quality, Map<String, String> attributes,
                                 double strengthPercent, String strengthBar) {
            this.item = item;
            this.quality = quality;
            this.attributes = Collections.unmodifiableMap(new LinkedHashMap<String, String>(attributes));
            this.strengthPercent = strengthPercent;
            this.strengthBar = strengthBar;
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

        public double getStrengthPercent() {
            return strengthPercent;
        }

        public String getStrengthBar() {
            return strengthBar;
        }
    }
}
