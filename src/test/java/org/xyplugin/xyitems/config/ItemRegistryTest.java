package org.xyplugin.xyitems.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;
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
        assertEquals(7, profile.getOutcomes().size());
        assertEquals(100D, profile.getTotalWeight(), 0.000001D);
        assertTrue(profile.getOutcomes().get(0).isFailure());
        assertEquals(30D, profile.getOutcomes().get(0).getProbability(), 0.000001D);
        assertEquals(6, definition.getQualities().size());
        assertTrue(definition.getQualities().containsKey("1"));
    }
}
