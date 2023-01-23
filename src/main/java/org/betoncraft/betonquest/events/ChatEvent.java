package org.betoncraft.betonquest.events;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

/**
 * Forces the player to run commands.
 */
@SuppressWarnings("PMD.CommentRequired")
public class ChatEvent extends QuestEvent {

    private final String[] messages;

    public ChatEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        final String string = instruction.getInstruction().trim();
        messages = string.substring(string.indexOf(' ') + 1).split("\\|");
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    protected Void execute(final String playerID) {
        final Player player = PlayerConverter.getPlayer(playerID);
        for (final String message : messages) {
            player.chat(message.replace("%player%", player.getName()));
        }
        return null;
    }

}
