package org.betonquest.betonquest.id.questlog;

import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.identifier.DefaultIdentifier;

/**
 * Identifies a quest in the {@code quests} section of a package.
 */
public class QuestLogIdentifier extends DefaultIdentifier {

    /**
     * The section quests are defined in.
     */
    public static final String QUESTS_SECTION = "quests";

    /**
     * Create a new quest identifier.
     *
     * @param pack       the package of the quest
     * @param identifier the name of the quest
     */
    protected QuestLogIdentifier(final QuestPackage pack, final String identifier) {
        super(pack, identifier);
    }
}
