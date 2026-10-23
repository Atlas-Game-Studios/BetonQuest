package org.betonquest.betonquest.mc_1_21_8;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.common.component.ComponentLineWrapper;
import org.betonquest.betonquest.api.dependency.CoreComponentLoader;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.conversation.ConversationColors;
import org.betonquest.betonquest.feature.questlog.QuestLog;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;
import org.betonquest.betonquest.mc_1_21_8.conversation.io.CustomClickAnswerListener;
import org.betonquest.betonquest.mc_1_21_8.conversation.io.DialogConvIOFactory;
import org.betonquest.betonquest.mc_1_21_8.questlog.QuestJournal;
import org.bukkit.Bukkit;

/**
 * Allows to register features with Minecraft 1.21.8.
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class BundledMC_1_21_8 implements Integration {

    /**
     * BetonQuest class to get relevant object from.
     */
    private final BetonQuest betonQuest;

    /**
     * Creates a new Object to register Minecraft version changes.
     *
     * @param betonQuest the BetonQuest class to get relevant object from
     */
    public BundledMC_1_21_8(final BetonQuest betonQuest) {
        this.betonQuest = betonQuest;
    }

    @Override
    public void enable(final BetonQuestApi api) {
        final CoreComponentLoader componentLoader = betonQuest.getComponentLoader();

        componentLoader.get(ConversationIORegistry.class).register("dialog", new DialogConvIOFactory(
                betonQuest.getPluginConfig(),
                componentLoader.get(ConversationColors.class),
                new ComponentLineWrapper(api.fonts())
        ));
        // Atlas
        api.bukkit().registerEvents(new CustomClickAnswerListener());
    }

    @Override
    public void postEnable(final BetonQuestApi betonQuestApi) {
        // Atlas: the quest journal is a MenuAPI dialog, so it needs MenuAPI on the server
        if (Bukkit.getPluginManager().isPluginEnabled("MenuAPI")) {
            final CoreComponentLoader componentLoader = betonQuest.getComponentLoader();
            final QuestLog questLog = componentLoader.get(QuestLog.class);
            final QuestJournal journal = new QuestJournal(betonQuestApi.loggerFactory().create(QuestJournal.class),
                    betonQuest.getPluginConfig().getConfig(), questLog, componentLoader.get(ProfileProvider.class));
            journal.check();
            questLog.setMenu(journal::open);
        }
    }

    @Override
    public void disable() {
        // Empty
    }
}
