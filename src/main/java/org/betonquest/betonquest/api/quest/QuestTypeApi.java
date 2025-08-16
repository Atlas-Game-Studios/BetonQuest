package org.betonquest.betonquest.api.quest;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.condition.ConditionID;
import org.betonquest.betonquest.api.quest.event.EventID;
import org.betonquest.betonquest.api.quest.objective.ObjectiveID;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * The QuestTypeApi provides access to the quest related core features.
 */
public interface QuestTypeApi {
    /**
     * Checks if the conditions described by conditionID are met.
     *
     * @param profile      the {@link Profile} of the player which should be checked
     * @param conditionIDs IDs of the conditions to check
     * @return if all conditions are met
     */
    boolean conditions(@Nullable Profile profile, Collection<ConditionID> conditionIDs);

    /**
     * Checks if the conditions described by conditionID are met.
     *
     * @param profile      the {@link Profile} of the player which should be checked
     * @param conditionIDs IDs of the conditions to check
     * @return if all conditions are met
     */
    boolean conditions(@Nullable Profile profile, ConditionID... conditionIDs);

    /**
     * Checks if the condition described by conditionID is met.
     *
     * @param conditionID ID of the condition to check
     * @param profile     the {@link Profile} of the player which should be checked
     * @return if the condition is met
     */
    boolean condition(@Nullable Profile profile, ConditionID conditionID);

    /**
     * Fires an event for the {@link Profile} if it meets the event's conditions.
     * If the profile is null, the event will be fired as a static event.
     *
     * @param profile the {@link Profile} for which the event must be executed or null
     * @param eventID ID of the event to fire
     * @return true if the event was run even if there was an exception during execution
     */
    boolean event(@Nullable Profile profile, EventID eventID);

    /**
     * Creates new objective for given player.
     *
     * @param profile     the {@link Profile} of the player
     * @param objectiveID ID of the objective
     */
    void newObjective(Profile profile, ObjectiveID objectiveID);

    /**
     * Resumes the existing objective for given player.
     *
     * @param profile     the {@link Profile} of the player
     * @param objectiveID ID of the objective
     * @param instruction data instruction string
     */
    void resumeObjective(Profile profile, ObjectiveID objectiveID, String instruction);

    /**
     * Renames the objective instance.
     *
     * @param name   the current name
     * @param rename the name it should have now
     */
    void renameObjective(ObjectiveID name, ObjectiveID rename);

    /**
     * Returns the list of objectives of this player.
     *
     * @param profile the {@link Profile} of the player
     * @return list of this player's active objectives
     */
    List<Objective> getPlayerObjectives(Profile profile);

    /**
     * Gets a stored Objective.
     *
     * @param objectiveID the id of the objective
     * @return the loaded Objective
     * @throws QuestException if no Objective is loaded for the ID
     */
    Objective getObjective(ObjectiveID objectiveID) throws QuestException;
}
