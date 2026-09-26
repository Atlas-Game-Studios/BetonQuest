package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.api.service.npc.NpcRegistry;

/**
 * Integrator for SimpleNPCs.
 */
public class SimpleNPCsIntegrator implements Integration {

    /**
     * The prefix used before any registered name for distinguishing.
     */
    public static final String PREFIX = "SimpleNPCs";

    /**
     * The empty default constructor.
     */
    public SimpleNPCsIntegrator() {
        // Empty
    }

    @Override
    public void enable(final BetonQuestApi api) {
        final NpcRegistry npcRegistry = api.npcs().registry();
        api.bukkit().registerEvents(new SimpleCatcher(api.profiles(), npcRegistry));
        api.bukkit().registerEvents(new SimpleHider(api.npcs().manager()));
        npcRegistry.register(PREFIX, new SimpleFactory());
        npcRegistry.registerIdentifier(new SimpleIdentifier(PREFIX));
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
