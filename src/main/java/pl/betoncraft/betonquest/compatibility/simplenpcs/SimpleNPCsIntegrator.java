package pl.betoncraft.betonquest.compatibility.simplenpcs;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.compatibility.Compatibility;
import pl.betoncraft.betonquest.compatibility.Integrator;
import pl.betoncraft.betonquest.exceptions.HookException;

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
    }

    @Override
    public void close() {

    }
}
