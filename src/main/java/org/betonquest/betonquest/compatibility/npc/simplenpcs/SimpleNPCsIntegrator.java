package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.api.quest.npc.NpcRegistry;
import org.betonquest.betonquest.compatibility.HookException;
import org.betonquest.betonquest.compatibility.Integrator;
import org.bukkit.Bukkit;

/**
 * Integrator implementation for the FancyNpcs plugin.
 */
public class SimpleNPCsIntegrator implements Integrator {
    /**
     * The prefix used before any registered name for distinguishing.
     */
    public static final String PREFIX = "SimpleNPCs";

    /**
     * The empty default Constructor.
     */
    public SimpleNPCsIntegrator() {
    }

    @Override
    public void hook(final BetonQuestApi api) throws HookException {
        final BetonQuest betonQuest = BetonQuest.getInstance();
        final NpcRegistry npcRegistry = betonQuest.getFeatureRegistries().npc();
        final ProfileProvider profileProvider = betonQuest.getProfileProvider();
        Bukkit.getPluginManager().registerEvents(new SimpleCatcher(profileProvider, npcRegistry), betonQuest);
        final SimpleHider hider = new SimpleHider(betonQuest.getFeatureApi().getNpcHider());
        Bukkit.getPluginManager().registerEvents(hider, betonQuest);
        npcRegistry.register(PREFIX, new SimpleFactory());
        npcRegistry.registerIdentifier(new SimpleIdentifier(PREFIX));
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
