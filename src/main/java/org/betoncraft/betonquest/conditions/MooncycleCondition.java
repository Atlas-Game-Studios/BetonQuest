package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

/**
 * This condition checks the players moon cycle (1 is full moon, 8 is Waxing Gibbous) and returns if the player is
 * under that moon.
 */
@SuppressWarnings("PMD.CommentRequired")
public class MooncycleCondition extends Condition {

    private final VariableNumber thisCycle;

    public MooncycleCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        this.thisCycle = instruction.getVarNum();
    }


    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final Player player = PlayerConverter.getPlayer(playerID);
        final int days = (int) player.getWorld().getFullTime() / 24_000;
        int phaseInt = days % 8;
        phaseInt += 1;
        return phaseInt == thisCycle.getInt(playerID);
    }

}



