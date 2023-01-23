package org.betonquest.betonquest.compatibility.simplenpcs;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.compatibility.Compatibility;
import org.betonquest.betonquest.compatibility.Integrator;
import org.betonquest.betonquest.exceptions.HookException;

import java.util.Arrays;

@SuppressWarnings("PMD.CommentRequired")
public class SimpleNPCsIntegrator implements Integrator {

    private final BetonQuest plugin;

    private SimpleNPCsListener simpleNPCsListener;

    public SimpleNPCsIntegrator() {
        plugin = BetonQuest.getInstance();
    }

    @Override
    public void hook() throws HookException {
        simpleNPCsListener = new SimpleNPCsListener();

        if (Compatibility.getHooked().contains("EffectLib")) {
            new SimpleNPCsParticle();
        }

        NPCHider.start();
        plugin.registerEvents("updatevisibility", UpdateVisibilityNowEvent.class);

        plugin.registerVariable("citizen", SimpleNPCsVariable.class);
        plugin.registerObjectives("npcinteract", NPCInteractObjective.class);
    }

    @Override
    public void reload() {
        if (Compatibility.getHooked().containsAll(Arrays.asList("SimpleNPCs", "EffectLib"))) {
            SimpleNPCsParticle.reload();
        }
        if (Compatibility.getHooked().contains("SimpleNPCs")) {
            simpleNPCsListener.reload();
        }
        if (NPCHider.getInstance() != null) {
            NPCHider.start();
        }
    }

    @Override
    public void close() {

    }
}
