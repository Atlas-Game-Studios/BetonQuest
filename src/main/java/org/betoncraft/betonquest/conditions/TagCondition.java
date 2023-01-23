package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.Utils;

/**
 * Requires the player to have specified tag
 */
@SuppressWarnings("PMD.CommentRequired")
public class TagCondition extends Condition {

    protected final String tag;

    public TagCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        tag = Utils.addPackage(instruction.getPackage(), instruction.next());
    }

    @Override
    protected Boolean execute(final String playerID) {
        return BetonQuest.getInstance().getPlayerData(playerID).hasTag(tag);
    }

}
