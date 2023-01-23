package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Checks if the player is gliding with elytra.
 */
@SuppressWarnings("PMD.CommentRequired")
public class FlyingCondition extends Condition {

    public FlyingCondition(final Instruction instruction) {
        super(instruction, true);
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        return PlayerConverter.getPlayer(playerID).isGliding();
    }

}
