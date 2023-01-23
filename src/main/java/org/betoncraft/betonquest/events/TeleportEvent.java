package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.conversation.Conversation;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.betoncraft.betonquest.utils.location.CompoundLocation;
import org.bukkit.Location;

/**
 * Teleports the player to specified location
 */
@SuppressWarnings("PMD.CommentRequired")
public class TeleportEvent extends QuestEvent {

    private final CompoundLocation loc;

    public TeleportEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        loc = instruction.getLocation();
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        final Conversation conv = Conversation.getConversation(playerID);
        if (conv != null) {
            conv.endConversation();
        }

        final Location playerLocation = loc.getLocation(playerID);
        PlayerConverter.getPlayer(playerID).teleport(playerLocation);
        return null;
    }
}
