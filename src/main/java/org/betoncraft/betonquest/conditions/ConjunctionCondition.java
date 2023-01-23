package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.id.ConditionID;

import java.util.List;

/**
 * All of specified conditions have to be true
 */
@SuppressWarnings("PMD.CommentRequired")
public class ConjunctionCondition extends Condition {

    private final List<ConditionID> conditions;

    public ConjunctionCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        conditions = instruction.getList(e -> instruction.getCondition(e));
    }

    @Override
    protected Boolean execute(final String playerID) {
        return BetonQuest.conditions(playerID, conditions);
    }
}
