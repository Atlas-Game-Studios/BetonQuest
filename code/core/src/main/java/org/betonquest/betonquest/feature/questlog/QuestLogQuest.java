package org.betonquest.betonquest.feature.questlog;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A quest of the quest log.
 *
 * @param name     the name of the quest
 * @param npc      who starts the quest, or null
 * @param prereqs  the full identifiers of the quests that must all be done first (empty if none)
 * @param category the category the quest is listed in
 * @param stages   the stages by name, in order
 */
public record QuestLogQuest(String name, @Nullable String npc, List<String> prereqs, String category,
                            Map<String, Stage> stages) {

    /**
     * A stage of a quest.
     *
     * @param name      the name of the stage
     * @param compass   the Cartography waypoint to point to, or null
     * @param objective what to do in this stage
     */
    public record Stage(String name, @Nullable String compass, String objective) {
    }
}
