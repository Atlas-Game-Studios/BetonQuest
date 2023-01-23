package org.betoncraft.betonquest.compatibility.denizen;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.compatibility.Integrator;


@SuppressWarnings("PMD.CommentRequired")
public class DenizenIntegrator implements Integrator {

    private final BetonQuest plugin;

    public DenizenIntegrator() {
        plugin = BetonQuest.getInstance();
    }

    @Override
    public void hook() {
        plugin.registerEvents("script", DenizenTaskScriptEvent.class);
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
