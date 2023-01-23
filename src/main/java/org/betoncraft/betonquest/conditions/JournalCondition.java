package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.Pointer;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.Utils;

/**
 * Checks if the player has specified pointer in his journal
 */
@SuppressWarnings("PMD.CommentRequired")
public class JournalCondition extends Condition {

    private final String targetPointer;

    public JournalCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        targetPointer = Utils.addPackage(instruction.getPackage(), instruction.next());
    }

    @Override
    protected Boolean execute(final String playerID) {
        for (final Pointer pointer : BetonQuest.getInstance().getPlayerData(playerID).getJournal().getPointers()) {
            if (pointer.getPointer().equalsIgnoreCase(targetPointer)) {
                return true;
            }
        }
        return false;
    }
}
