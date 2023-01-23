package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

/**
 * Requires the player to have specified level of experience (or more)
 */
@SuppressWarnings("PMD.CommentRequired")
public class ExperienceCondition extends Condition {

    private final VariableNumber amount;
    private final boolean checkForLevel;

    public ExperienceCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        this.amount = instruction.getVarNum();
        this.checkForLevel = instruction.hasArgument("level");
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final Player player = PlayerConverter.getPlayer(playerID);
        final int amount = this.amount.getInt(playerID);
        if (checkForLevel) {
            return player.getLevel() >= amount;
        } else {
            return player.getTotalExperience() >= amount;
        }
    }

}
