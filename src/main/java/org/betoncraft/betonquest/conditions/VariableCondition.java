package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;

/**
 * Checks if the variable value matches given pattern.
 */
@SuppressWarnings("PMD.CommentRequired")
public class VariableCondition extends Condition {

    private final String variable;
    private final String regex;

    public VariableCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        variable = instruction.next();
        regex = instruction.next().replace('_', ' ');
    }

    @Override
    protected Boolean execute(final String playerID) {
        return BetonQuest.getInstance().getVariableValue(instruction.getPackage().getName(), variable, playerID).matches(regex);
    }

}
