package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;

/**
 * Requires a specified amount of global points (or more) in specified
 * category
 */
@SuppressWarnings("PMD.CommentRequired")
public class GlobalPointCondition extends PointCondition {

    public GlobalPointCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction);
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        return check(playerID, BetonQuest.getInstance().getGlobalData().getPoints());
    }

}
