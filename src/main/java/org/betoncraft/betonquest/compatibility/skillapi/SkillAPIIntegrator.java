package org.betoncraft.betonquest.compatibility.skillapi;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.compatibility.Integrator;


@SuppressWarnings("PMD.CommentRequired")
public class SkillAPIIntegrator implements Integrator {

    private final BetonQuest plugin;

    public SkillAPIIntegrator() {
        plugin = BetonQuest.getInstance();
    }

    @Override
    public void hook() {
        plugin.registerConditions("skillapiclass", SkillAPIClassCondition.class);
        plugin.registerConditions("skillapilevel", SkillAPILevelCondition.class);
        new SkillAPIKillListener();
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
