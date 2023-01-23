package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.Utils;

/**
 * Cancels the quest for the player.
 */
@SuppressWarnings("PMD.CommentRequired")
public class CancelEvent extends QuestEvent {

    private final String canceler;

    public CancelEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        canceler = Utils.addPackage(instruction.getPackage(), instruction.next());
    }

    @Override
    protected Void execute(final String playerID) {
        BetonQuest.getInstance().getPlayerData(playerID).cancelQuest(canceler);
        return null;
    }

}
