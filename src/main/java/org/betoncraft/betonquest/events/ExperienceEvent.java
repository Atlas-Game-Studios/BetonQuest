package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

/**
 * Gives the player specified amount of experience
 */
@SuppressWarnings("PMD.CommentRequired")
public class ExperienceEvent extends QuestEvent {

    private final VariableNumber amount;
    private final boolean checkForLevel;

    public ExperienceEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        this.amount = instruction.getVarNum();
        this.checkForLevel = instruction.hasArgument("level");
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        final Player player = PlayerConverter.getPlayer(playerID);
        final int amount = this.amount.getInt(playerID);
        if (checkForLevel) {
            player.giveExpLevels(amount);
        } else {
            player.giveExp(amount);
        }
        return null;
    }
}
