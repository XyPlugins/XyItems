package org.xyplugin.xyitems.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.junit.Test;
import org.xyplugin.xyitems.api.ForgeOutcomeProfile;

public class ItemRegistryTest {
    @Test
    public void defaultForgeExampleHasOneFinalHundredWeightSnapshot() {
        ItemRegistry.LoadResult loaded = ItemRegistry.load(new File("src/main/resources/items"),
                Logger.getLogger("ItemRegistryTest"));
        assertTrue(loaded.getErrors().toString(), loaded.isSuccess());
        assertEquals(3, loaded.getRegistry().size());

        ItemDefinition definition = loaded.getRegistry().find("example_forge_soul").get();
        Optional<ForgeOutcomeProfile> optional = definition.createForgeOutcomeProfile();
        assertTrue(optional.isPresent());
        ForgeOutcomeProfile profile = optional.get();
        assertEquals(9, profile.getOutcomes().size());
        assertEquals(100D, profile.getTotalWeight(), 0.000001D);
        assertTrue(profile.getOutcomes().get(0).isFailure());
        assertEquals(20D, profile.getOutcomes().get(0).getProbability(), 0.000001D);
        assertEquals(8, definition.getQualities().size());
        assertTrue(definition.getQualities().containsKey("白描"));
    }

    @Test
    public void sharedIdentifyTemplateSupportsNamedQualitiesAndArbitraryAttributes() {
        ItemRegistry.LoadResult loaded = ItemRegistry.load(new File("src/main/resources/items"),
                Logger.getLogger("ItemRegistryTest"));
        assertTrue(loaded.getErrors().toString(), loaded.isSuccess());

        QualityDefinition quality = loaded.getRegistry().find("example_forge_soul").get()
                .getQualities().get("群青");
        assertEquals("群青", quality.getName());
        assertEquals("<品质.颜色>示例墨魂", quality.getDisplayName());
        assertTrue(quality.getLore().contains("&7撕裂: &c+<撕裂>"));

        Map<String, String> rolled = quality.rollAttributes();
        assertTrue(rolled.containsKey("damage"));
        assertTrue(rolled.containsKey("health"));
        assertTrue(rolled.containsKey("撕裂"));
        assertTrue(rolled.containsKey("暴击率"));

        ItemDefinition definition = loaded.getRegistry().find("example_forge_soul").get();
        assertTrue(definition.isUnbreakable());
        assertTrue(definition.shouldHideUnbreakable());
    }

    @Test
    public void oneQualityAndZeroFailureProducesGuaranteedSuccessProfile() {
        Map<String, QualityDefinition> qualities = new LinkedHashMap<String, QualityDefinition>();
        qualities.put("legendary", new QualityDefinition("legendary", "传说", "&6", 1D,
                "&6传说之剑", Collections.singletonList("&7必定成功"),
                Collections.<String, NumberRange>emptyMap()));
        ItemDefinition definition = new ItemDefinition("guaranteed_legendary", Material.DIAMOND_SWORD,
                (short) 0, "&f未鉴定之剑", Collections.<String>emptyList(), qualities,
                new ForgeFailureDefinition(0D, "锻造失败", "&c"));

        ForgeOutcomeProfile profile = definition.createForgeOutcomeProfile().get();
        assertEquals(2, profile.getOutcomes().size());
        assertEquals(1D, profile.getTotalWeight(), 0.000001D);
        assertTrue(profile.getOutcomes().get(0).isFailure());
        assertEquals(0D, profile.getOutcomes().get(0).getProbability(), 0.000001D);
        assertEquals("传说", profile.getOutcomes().get(1).getName());
        assertEquals(100D, profile.getOutcomes().get(1).getProbability(), 0.000001D);
    }

    @Test
    public void explicitZeroFailureYamlLoadsButMissingWeightIsRejected() {
        ItemRegistry.LoadResult valid = ItemRegistry.load(new File("src/test/resources/zero-failure"),
                Logger.getLogger("zero-failure"));
        assertTrue(valid.getErrors().toString(), valid.isSuccess());
        ForgeOutcomeProfile profile = valid.getRegistry().find("guaranteed_legendary").get()
                .createForgeOutcomeProfile().get();
        assertEquals(0D, profile.getOutcomes().get(0).getProbability(), 0.000001D);
        assertEquals(100D, profile.getOutcomes().get(1).getProbability(), 0.000001D);

        ItemRegistry.LoadResult invalid = ItemRegistry.load(new File("src/test/resources/missing-failure-weight"),
                Logger.getLogger("missing-failure-weight"));
        assertFalse(invalid.isSuccess());
        assertTrue(invalid.getErrors().toString(), invalid.getErrors().get(0).contains("必须明确配置 weight"));
    }
}
