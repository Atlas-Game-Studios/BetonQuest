package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.conversation.Conversation;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.betoncraft.betonquest.utils.Utils;

/**
 * Fires the conversation for the player
 */
@SuppressWarnings("PMD.CommentRequired")
public class ConversationEvent extends QuestEvent {

    private final String conv;

    public ConversationEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        conv = Utils.addPackage(instruction.getPackage(), instruction.next());
    }

    @Override
    protected Void execute(final String playerID) {
        new Conversation(playerID, conv, PlayerConverter.getPlayer(playerID).getLocation());
        return null;
    }
}
