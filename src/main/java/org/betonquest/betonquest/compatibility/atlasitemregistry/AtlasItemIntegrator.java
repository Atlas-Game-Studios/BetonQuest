package org.betonquest.betonquest.compatibility.atlasitemregistry;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.compatibility.Integrator;
import org.betonquest.betonquest.item.ItemRegistry;

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
    public void hook(final BetonQuestApi api) {
        final BetonQuest plugin = BetonQuest.getInstance();
        final ItemRegistry itemRegistry = plugin.getFeatureRegistries().item();
        itemRegistry.register("registry", new AtlasQuestItemFactory());
        itemRegistry.registerSerializer("registry", new AtlasQuestItemSerializer());
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
