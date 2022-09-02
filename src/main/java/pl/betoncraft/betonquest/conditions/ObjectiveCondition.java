package pl.betoncraft.betonquest.conditions;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.id.ObjectiveID;

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
