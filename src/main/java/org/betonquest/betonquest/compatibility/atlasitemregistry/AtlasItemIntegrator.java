package org.betonquest.betonquest.compatibility.atlasitemregistry;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.compatibility.Integrator;
import org.betonquest.betonquest.kernel.registry.feature.ItemTypeRegistry;

/**
 * Integrator for Atlas Items.
 */
public class AtlasItemIntegrator implements Integrator {

    /**
     * The default constructor.
     */
    public AtlasItemIntegrator() {

    }

    @Override
    public void hook() {
        final BetonQuest plugin = BetonQuest.getInstance();
        final ItemTypeRegistry itemTypes = plugin.getFeatureRegistries().item();
        itemTypes.register("registry", new AtlasQuestItemFactory());
        itemTypes.registerSerializer("registry", new AtlasQuestItemSerializer());
    }

    @Override
    public void reload() {
        // Empty
    }

    @Override
    public void close() {
        // Empty
    }
}
