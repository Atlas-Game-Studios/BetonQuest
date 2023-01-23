package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

/**
 * Simply kills the player.
 */
@SuppressWarnings("PMD.CommentRequired")
public class KillEvent extends QuestEvent {

    public KillEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
    }

    @Override
    protected Void execute(final String playerID) {
        final Player player = PlayerConverter.getPlayer(playerID);
        player.damage(player.getHealth() + 1);
        return null;
    }

}
