package org.betonquest.betonquest.compatibility.atlasitemregistry;

import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.lib.integration.IntegrationTemplate;

/**
 * Integrator for AtlasItemRegistry.
 */
public class AtlasItemIntegrator extends IntegrationTemplate {

    /**
     * The empty default constructor.
     */
    public AtlasItemIntegrator() {
        super();
    }

    @Override
    public void enable(final BetonQuestApi api) {
        item("registry", new AtlasQuestItemFactory(), new AtlasQuestItemSerializer());

        registerFeatures(api);
    }

    @Override
    public void postEnable(final BetonQuestApi api) {
        // Empty
    }

    @Override
    public void disable() {
        // Empty
    }
}
