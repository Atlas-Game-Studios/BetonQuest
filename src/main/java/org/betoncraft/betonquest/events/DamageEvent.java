package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Damages the player
 */
@SuppressWarnings("PMD.CommentRequired")
public class DamageEvent extends QuestEvent {

    private final VariableNumber damage;

    public DamageEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        damage = instruction.getVarNum();
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        PlayerConverter.getPlayer(playerID).damage(Math.abs(damage.getDouble(playerID)));
        return null;
    }

}
