package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("PMD.CommentRequired")
public class NotifyAllEvent extends NotifyEvent {

    public NotifyAllEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction);
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            super.execute(PlayerConverter.getID(player));
        }
        return null;
    }
}
