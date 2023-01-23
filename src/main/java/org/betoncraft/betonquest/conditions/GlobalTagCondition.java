package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.exceptions.InstructionParseException;

/**
 * Requires the specified global tag to be set
 */
@SuppressWarnings("PMD.CommentRequired")
public class GlobalTagCondition extends TagCondition {

    public GlobalTagCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        staticness = true;
        persistent = true;
    }

    @Override
    protected Boolean execute(final String playerID) {
        return BetonQuest.getInstance().getGlobalData().hasTag(tag);
    }

}
