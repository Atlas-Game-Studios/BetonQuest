package org.betonquest.betonquest.id.questlog;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.config.quest.QuestPackageManager;
import org.betonquest.betonquest.api.identifier.factory.DefaultIdentifierFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Factory to create {@link QuestLogIdentifier}s.
 */
public class QuestLogIdentifierFactory extends DefaultIdentifierFactory<QuestLogIdentifier> {

    /**
     * Create a new quest identifier factory.
     *
     * @param packManager the quest package manager to get quest packages from
     */
    public QuestLogIdentifierFactory(final QuestPackageManager packManager) {
        super(packManager, "Quest");
    }

    @Override
    public QuestLogIdentifier parseIdentifier(@Nullable final QuestPackage source, final String input) throws QuestException {
        final Map.Entry<QuestPackage, String> entry = parse(source, input);
        return requireSection(new QuestLogIdentifier(entry.getKey(), entry.getValue()), QuestLogIdentifier.QUESTS_SECTION);
    }
}
