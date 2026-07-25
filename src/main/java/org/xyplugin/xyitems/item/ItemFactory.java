package org.xyplugin.xyitems.item;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xycore.api.item.ItemTagService;
import org.xyplugin.xyitems.config.ItemDefinition;
import org.xyplugin.xyitems.integration.XyCoreBridge;

/** Builds and recognizes authentic XyItems stacks through XyCore's NBT abstraction. */
public final class ItemFactory {
    public static final String ITEM_ID_TAG = "xyitems-id";
    public static final String STATE_TAG = "xyitems-state";
    public static final String QUALITY_TAG = "xyitems-quality";
    public static final String ATTRIBUTES_TAG = "xyitems-attributes";
    public static final String STATE_UNIDENTIFIED = "unidentified";
    public static final String STATE_READY = "ready";
    public static final String STATE_IDENTIFIED = "identified";

    private final XyCoreBridge core;

    public ItemFactory(XyCoreBridge core) {
        this.core = core;
    }

    public Optional<ItemStack> createBase(ItemDefinition definition, int amount) {
        if (definition == null || amount <= 0) return Optional.empty();
        ItemStack item = definition.createUnidentified(amount);
        ItemTagService tags = core.getItemTags();
        item = tags.setString(item, ITEM_ID_TAG, definition.getId());
        item = tags.setString(item, STATE_TAG, definition.isIdentifiable() ? STATE_UNIDENTIFIED : STATE_READY);
        return Optional.of(item);
    }

    public Optional<IdentificationResult> identify(ItemStack source, ItemDefinition definition) {
        if (source == null || definition == null || !definition.isIdentifiable() || !isUnidentified(source, definition)) {
            return Optional.empty();
        }
        ItemDefinition.IdentifiedResult built = definition.createIdentified(1);
        if (built == null) return Optional.empty();

        ItemTagService tags = core.getItemTags();
        ItemStack item = built.getItem();
        item = tags.setString(item, ITEM_ID_TAG, definition.getId());
        item = tags.setString(item, STATE_TAG, STATE_IDENTIFIED);
        item = tags.setString(item, QUALITY_TAG, built.getQuality().getId());
        item = tags.setString(item, ATTRIBUTES_TAG, encodeAttributes(built.getAttributes()));
        return Optional.of(new IdentificationResult(item, built.getQuality().getId(), built.getQuality().getName()));
    }

    public Optional<String> getItemId(ItemStack item) {
        return core.getItemTags().getString(item, ITEM_ID_TAG);
    }

    public Optional<String> getQualityId(ItemStack item) {
        return core.getItemTags().getString(item, QUALITY_TAG);
    }

    /** Returns the exact rolls saved during identification rather than trying to parse display Lore. */
    public Map<String, String> getRolledAttributes(ItemStack item) {
        Optional<String> serialized = core.getItemTags().getString(item, ATTRIBUTES_TAG);
        if (!serialized.isPresent() || serialized.get().isEmpty()) return Collections.emptyMap();

        Map<String, String> attributes = new LinkedHashMap<String, String>();
        String[] entries = serialized.get().split(",", -1);
        for (String entry : entries) {
            int separator = entry.indexOf(':');
            if (separator <= 0 || separator >= entry.length() - 1) continue;
            try {
                String key = decode(entry.substring(0, separator));
                String value = decode(entry.substring(separator + 1));
                if (!key.isEmpty()) attributes.put(key, value);
            } catch (IllegalArgumentException ignored) {
                // A malformed NBT value must not break unrelated player interactions.
            }
        }
        return Collections.unmodifiableMap(attributes);
    }

    public boolean isUnidentified(ItemStack item, ItemDefinition definition) {
        if (item == null || definition == null) return false;
        Optional<String> itemId = getItemId(item);
        if (!itemId.isPresent() || !definition.getId().equalsIgnoreCase(itemId.get())) return false;
        return STATE_UNIDENTIFIED.equalsIgnoreCase(core.getItemTags()
                .getString(item, STATE_TAG).orElse(""));
    }

    private String encodeAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) return "";
        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (encoded.length() > 0) encoded.append(',');
            encoded.append(encode(entry.getKey())).append(':').append(encode(entry.getValue()));
        }
        return encoded.toString();
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                (value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public static final class IdentificationResult {
        private final ItemStack item;
        private final String qualityId;
        private final String qualityName;

        private IdentificationResult(ItemStack item, String qualityId, String qualityName) {
            this.item = item;
            this.qualityId = qualityId;
            this.qualityName = qualityName;
        }

        public ItemStack getItem() {
            return item;
        }

        public String getQualityId() {
            return qualityId;
        }

        public String getQualityName() {
            return qualityName;
        }
    }
}
