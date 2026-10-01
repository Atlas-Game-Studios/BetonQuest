package org.betonquest.betonquest.feature.questlog;

/**
 * A profile's progress in one quest of the quest log.
 *
 * @param stage    the current stage of the quest
 * @param complete whether the quest is complete
 * @param tracked  when the quest was tracked on the sidebar in epoch millis, 0 if it is not tracked
 */
public record QuestLogEntry(String stage, boolean complete, long tracked) {

    /**
     * Whether the quest is tracked on the sidebar.
     *
     * @return true if tracked
     */
    public boolean isTracked() {
        return tracked != 0;
    }
}
