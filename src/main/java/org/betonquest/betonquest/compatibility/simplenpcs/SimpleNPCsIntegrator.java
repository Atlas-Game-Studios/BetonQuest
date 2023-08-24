package org.betonquest.betonquest.compatibility.simplenpcs;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
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
        final BetonQuestLoggerFactory loggerFactory = BetonQuest.getInstance().getLoggerFactory();
        simpleNPCsListener = new SimpleNPCsListener(loggerFactory, loggerFactory.create(SimpleNPCsListener.class));

        NPCHider.start(loggerFactory.create(NPCHider.class));
        plugin.registerEvents("updatevisibility", UpdateVisibilityNowEvent.class);

        plugin.registerVariable("citizen", SimpleNPCsVariable.class);
        plugin.registerObjectives("npcinteract", NPCInteractObjective.class);
    }

    @Override
    public void reload() {
        simpleNPCsListener.reload();
        if (NPCHider.getInstance() != null) {
            NPCHider.start(BetonQuest.getInstance().getLoggerFactory().create(NPCHider.class));
        }
    }

    @Override
    public void close() {

    }
}
