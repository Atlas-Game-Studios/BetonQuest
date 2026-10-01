package org.betonquest.betonquest.quest.action.questlog;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.action.OnlineAction;
import org.betonquest.betonquest.feature.questlog.QuestLog;
import org.jetbrains.annotations.Nullable;

/**
 * Adds, advances, completes or deletes a quest in the quest log.
 *
 * @param questLog  the quest log to change
 * @param operation one of add, set, complete and delete
 * @param quest     the full quest identifier
 * @param stage     the stage for add and set, null otherwise
 */
public record QuestLogAction(QuestLog questLog, String operation, String quest, @Nullable String stage) implements OnlineAction {

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        switch (operation) {
            case "add" -> questLog.add(profile, quest, requireStage());
            case "set" -> questLog.set(profile, quest, requireStage());
            case "complete" -> questLog.complete(profile, quest);
            case "delete" -> questLog.delete(profile, quest);
            default -> throw new QuestException("Unknown questlog operation: " + operation);
        }
    }

    private String requireStage() throws QuestException {
        if (stage == null) {
            throw new QuestException("questlog " + operation + " needs a stage");
        }
        return stage;
    }

    @Override
    public boolean isPrimaryThreadEnforced() {
        return true;
    }
}
