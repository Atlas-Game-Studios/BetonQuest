package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Requires the player to have specified amount of health (or more)
 */
@SuppressWarnings("PMD.CommentRequired")
public class HealthCondition extends Condition {

    private final VariableNumber health;

    public HealthCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        health = instruction.getVarNum();
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        return PlayerConverter.getPlayer(playerID).getHealth() >= health.getDouble(playerID);
    }

}
