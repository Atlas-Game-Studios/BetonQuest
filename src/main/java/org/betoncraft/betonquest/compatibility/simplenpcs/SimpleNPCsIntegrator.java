package org.betoncraft.betonquest.compatibility.simplenpcs;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.compatibility.Compatibility;
import org.betoncraft.betonquest.compatibility.Integrator;
import org.betoncraft.betonquest.exceptions.HookException;

import java.util.Arrays;

public class SimpleNPCsIntegrator implements Integrator {

    private final BetonQuest plugin;

    private SimpleNPCsListener simpleNPCsListener;

    public SimpleNPCsIntegrator() {
        plugin = BetonQuest.getInstance();
    }

    @Override
    public void hook() throws HookException {
        simpleNPCsListener = new SimpleNPCsListener();

        if (Compatibility.getHooked().contains("HolographicDisplays")) {
            new SimpleNPCsHologram();
        }

        NPCHider.start();
        plugin.registerEvents("updatevisibility", UpdateVisibilityNowEvent.class);

        plugin.registerVariable("citizens", SimpleNPCsVariable.class);
        plugin.registerObjectives("npcinteract", NPCInteractObjective.class);
    }

    @Override
    public void reload() {
        if (Compatibility.getHooked().containsAll(Arrays.asList("SimpleNPCs", "HolographicDisplays"))) {
            SimpleNPCsHologram.reload();
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
