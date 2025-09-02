package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.compatibility.HookException;
import org.betonquest.betonquest.compatibility.Integrator;
import org.betonquest.betonquest.kernel.registry.quest.NpcTypeRegistry;
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
    public void hook() throws HookException {
        final BetonQuest betonQuest = BetonQuest.getInstance();
        final NpcTypeRegistry npcTypes = betonQuest.getFeatureRegistries().npc();
        final ProfileProvider profileProvider = betonQuest.getProfileProvider();
        Bukkit.getPluginManager().registerEvents(new SimpleCatcher(profileProvider, npcTypes), betonQuest);
        final SimpleHider hider = new SimpleHider(betonQuest.getFeatureApi().getNpcHider());
        Bukkit.getPluginManager().registerEvents(hider, betonQuest);
        npcTypes.register(PREFIX, new SimpleFactory());
        npcTypes.registerIdentifier(new SimpleIdentifier(PREFIX));
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
