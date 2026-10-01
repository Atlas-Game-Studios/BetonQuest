package org.betonquest.betonquest.feature.questlog;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.identifier.Identifier;
import org.betonquest.betonquest.api.instruction.section.SectionInstruction;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.service.instruction.Instructions;
import org.betonquest.betonquest.id.questlog.QuestLogIdentifier;
import org.betonquest.betonquest.id.questlog.QuestLogIdentifierFactory;
import org.betonquest.betonquest.kernel.processor.SectionProcessor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads {@link QuestLogQuest}s from the {@code quests} section of packages.
 */
public class QuestLogProcessor extends SectionProcessor<QuestLogIdentifier, QuestLogQuest> {

    /**
     * Create a new quest log processor.
     *
     * @param log               the custom logger for this class
     * @param instructionApi    the instruction api to use
     * @param identifierFactory the identifier factory to create {@link QuestLogIdentifier}s
     */
    public QuestLogProcessor(final BetonQuestLogger log, final Instructions instructionApi,
                             final QuestLogIdentifierFactory identifierFactory) {
        super(log, instructionApi, identifierFactory, "Quest", QuestLogIdentifier.QUESTS_SECTION);
    }

    @Override
    protected Map.Entry<QuestLogIdentifier, QuestLogQuest> loadSection(final String sectionName, final SectionInstruction instruction) throws QuestException {
        final QuestPackage pack = instruction.getPackage();
        final ConfigurationSection section = instruction.getSection();
        final ConfigurationSection stageSection = section.getConfigurationSection("stages");
        if (stageSection == null) {
            throw new QuestException("Quest '" + sectionName + "' has no stages");
        }
        final Map<String, QuestLogQuest.Stage> stages = new LinkedHashMap<>();
        for (final String stage : stageSection.getKeys(false)) {
            stages.put(stage, new QuestLogQuest.Stage(required(stageSection, stage + ".name"),
                    stageSection.getString(stage + ".compass"), required(stageSection, stage + ".objective")));
        }
        final String prereq = section.getString("prereq");
        final QuestLogQuest quest = new QuestLogQuest(required(section, "name"), section.getString("npc"),
                prereq == null ? null : fullId(pack, prereq), required(section, "category"), stages);
        return Map.entry(getIdentifier(pack, sectionName), quest);
    }

    private String required(final ConfigurationSection section, final String path) throws QuestException {
        final String value = section.getString(path);
        if (value == null) {
            throw new QuestException("Missing '" + path + "'");
        }
        return value;
    }

    private String fullId(final QuestPackage pack, final String quest) {
        return quest.contains(Identifier.SEPARATOR) ? quest : pack.getQuestPath() + Identifier.SEPARATOR + quest;
    }

    /**
     * Get a quest by its full identifier.
     *
     * @param fullId the full identifier, e.g. {@code Package>quest}
     * @return the quest or null if there is none
     */
    @Nullable
    public QuestLogQuest get(final String fullId) {
        for (final Map.Entry<QuestLogIdentifier, QuestLogQuest> entry : values.entrySet()) {
            if (entry.getKey().getFull().equals(fullId)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
