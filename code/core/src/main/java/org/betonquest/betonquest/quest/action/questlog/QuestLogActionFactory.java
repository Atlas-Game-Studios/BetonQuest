package org.betonquest.betonquest.quest.action.questlog;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.quest.action.OnlineActionAdapter;
import org.betonquest.betonquest.api.quest.action.PlayerAction;
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory;
import org.betonquest.betonquest.feature.questlog.QuestLog;
import org.betonquest.betonquest.id.questlog.QuestLogIdentifier;

import java.util.Locale;
import java.util.Set;

/**
 * Factory for the {@code questlog add|set <quest> <stage>} and {@code questlog complete|delete <quest>} actions.
 */
public class QuestLogActionFactory implements PlayerActionFactory {

    /**
     * The operations that take a stage.
     */
    private static final Set<String> WITH_STAGE = Set.of("add", "set");

    /**
     * The quest log to change.
     */
    private final QuestLog questLog;

    /**
     * Create a new quest log action factory.
     *
     * @param questLog the quest log to change
     */
    public QuestLogActionFactory(final QuestLog questLog) {
        this.questLog = questLog;
    }

    @Override
    public PlayerAction parsePlayer(final Instruction instruction) throws QuestException {
        final String operation = instruction.string().get().getValue(null).toLowerCase(Locale.ROOT);
        if (!WITH_STAGE.contains(operation) && !"complete".equals(operation) && !"delete".equals(operation)) {
            throw new QuestException("Unknown questlog operation: " + operation);
        }
        final QuestLogIdentifier questId = instruction.identifier(QuestLogIdentifier.class).get().getValue(null);
        final String stage = WITH_STAGE.contains(operation) ? stage(instruction, questId) : null;
        return new OnlineActionAdapter(new QuestLogAction(questLog, operation, questId.getFull(), stage));
    }

    private String stage(final Instruction instruction, final QuestLogIdentifier questId) throws QuestException {
        final String stage = instruction.string().get().getValue(null);
        if (!questId.getPackage().getConfig().isConfigurationSection(QuestLogIdentifier.QUESTS_SECTION + "." + questId.get() + ".stages." + stage)) {
            throw new QuestException("Quest '" + questId.getFull() + "' has no stage '" + stage + "'");
        }
        return stage;
    }
}
