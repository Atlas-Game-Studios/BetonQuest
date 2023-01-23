package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Requires the player to have specified permission node
 */
@SuppressWarnings("PMD.CommentRequired")
public class PermissionCondition extends Condition {

    private final String permission;

    public PermissionCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        permission = instruction.next();
    }

    @Override
    protected Boolean execute(final String playerID) {
        return PlayerConverter.getPlayer(playerID).hasPermission(permission);
    }

}
