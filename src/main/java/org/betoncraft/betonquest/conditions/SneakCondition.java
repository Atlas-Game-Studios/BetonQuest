package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Returns true if the player is sneaking
 */
@SuppressWarnings("PMD.CommentRequired")
public class SneakCondition extends Condition {

    public SneakCondition(final Instruction instruction) {
        super(instruction, true);
    }

    @Override
    protected Boolean execute(final String playerID) {
        return PlayerConverter.getPlayer(playerID).isSneaking();
    }

}
