package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.betoncraft.betonquest.utils.location.CompoundLocation;

/**
 * Checks Y height player is at (must be below)
 */
@SuppressWarnings("PMD.CommentRequired")
public class HeightCondition extends Condition {

    private final VariableNumber height;

    public HeightCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        final String string = instruction.next();
        final String packName = instruction.getPackage().getName();
        if (string.matches("\\-?\\d+\\.?\\d*")) {
            try {
                height = new VariableNumber(packName, string);
            } catch (InstructionParseException e) {
                throw new InstructionParseException("Could not parse height", e);
            }
        } else {
            try {
                height = new VariableNumber(new CompoundLocation(packName, string).getLocation(null).getY());
            } catch (QuestRuntimeException e) {
                throw new InstructionParseException("Could not parse height", e);
            }
        }
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        return PlayerConverter.getPlayer(playerID).getLocation().getY() < height.getDouble(playerID);
    }

}
