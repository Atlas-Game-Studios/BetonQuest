package org.betoncraft.betonquest.compatibility.mmogroup.mmolib;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.compatibility.Integrator;

@SuppressWarnings("PMD.CommentRequired")
public class MMOLibIntegrator implements Integrator {

    private final BetonQuest plugin;

    public MMOLibIntegrator() {
        plugin = BetonQuest.getInstance();
    }

    @Override
    public void hook() {
        plugin.registerConditions("mmostat", MMOLibStatCondition.class);
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
