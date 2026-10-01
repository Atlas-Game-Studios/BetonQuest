package org.betonquest.betonquest.compatibility.menuapi;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.feature.questlog.QuestLog;

/**
 * Opens the quest log in MenuAPI menus.
 */
public class MenuApiIntegrator implements Integration {

    /**
     * Create a new MenuAPI integrator.
     */
    public MenuApiIntegrator() {
    }

    @Override
    public void enable(final BetonQuestApi api) {
        // Empty
    }

    @Override
    public void postEnable(final BetonQuestApi api) {
        final BetonQuest plugin = BetonQuest.getInstance();
        final QuestLog questLog = plugin.getComponentLoader().get(QuestLog.class);
        questLog.setMenu(new QuestLogMenus(plugin, plugin.getPluginConfig(), questLog,
                plugin.getComponentLoader().get(ProfileProvider.class))::openTop);
    }

    @Override
    public void disable() {
        // Empty
    }
}
