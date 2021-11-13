package pl.betoncraft.betonquest.compatibility.simplenpcs;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.compatibility.Integrator;
import pl.betoncraft.betonquest.exceptions.HookException;

public class SimpleNPCsIntegrator implements Integrator {

    private final BetonQuest plugin;

    private SimpleNPCsListener simpleNPCsListener;

    public SimpleNPCsIntegrator() {
        plugin = BetonQuest.getInstance();
    }

    @Override
    public void hook() throws HookException {
        simpleNPCsListener = new SimpleNPCsListener();

        plugin.registerVariable("citizens", SimpleNPCsVariable.class);
        plugin.registerObjectives("npcinteract", NPCInteractObjective.class);
    }

    @Override
    public void reload() {

    }

    @Override
    public void close() {

    }
}
