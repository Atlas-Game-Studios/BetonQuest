package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.util.Random;

/**
 * The condition that is met randomly.
 */
@SuppressWarnings("PMD.CommentRequired")
public class RandomCondition extends Condition {

    private final Random random = new Random();
    private final VariableNumber valueMax;
    private final VariableNumber rangeOfRandom;

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public RandomCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        staticness = true;
        persistent = true;
        final String[] values = instruction.next().split("-");
        if (values.length != 2) {
            throw new InstructionParseException("Wrong randomness format");
        }
        final String packName = instruction.getPackage().getName();
        try {
            valueMax = new VariableNumber(packName, values[0]);
            rangeOfRandom = new VariableNumber(packName, values[1]);
        } catch (InstructionParseException e) {
            throw new InstructionParseException("Cannot parse randomness values", e);
        }
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final int temp = random.nextInt(rangeOfRandom.getInt(playerID)) + 1;
        return temp <= valueMax.getInt(playerID);
    }

}
