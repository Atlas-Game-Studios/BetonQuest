package org.betonquest.betonquest.feature.questlog;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A quest of the quest log.
 *
 * @param name     the name of the quest
 * @param npc      who starts the quest, or null
 * @param prereq   the full identifier of the quest that must be done first, or null
 * @param category the category the quest is listed in
 * @param stages   the stages by name, in order
 */
public record QuestLogQuest(String name, @Nullable String npc, @Nullable String prereq, String category,
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
