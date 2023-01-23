package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.api.Objective;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.id.ObjectiveID;

/**
 * Checks if the player has specified objective active.
 */
@SuppressWarnings("PMD.CommentRequired")
public class ObjectiveCondition extends Condition {

    public final ObjectiveID objective;

    public ObjectiveCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        objective = instruction.getObjective();
        if (objective == null) {
            throw new InstructionParseException("Objective does not exist");
        }
    }

    @Override
    protected Boolean execute(final String playerID) {
        Objective o = BetonQuest.getInstance().getObjective(objective);
        if (o == null) return false;
        return o.containsPlayer(playerID);
    }

}
