package org.betonquest.betonquest.compatibility.simplemagic;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.compatibility.Integrator;
import org.betonquest.betonquest.compatibility.magic.WandCondition;

@SuppressWarnings("PMD.CommentRequired")
public class SimpleMagicIntegrator implements Integrator {

    private final BetonQuest plugin;

    public SimpleMagicIntegrator() {
        plugin = BetonQuest.getInstance();
    }

    @Override
    public void hook() {
        plugin.registerConditions("hasdud", HasDudCondition.class);
        plugin.registerConditions("hasspell", HasSpellCondition.class);
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
