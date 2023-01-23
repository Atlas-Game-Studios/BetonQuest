package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.database.GlobalData;
import org.betoncraft.betonquest.exceptions.InstructionParseException;

/**
 * Adds or removes global tags
 */
@SuppressWarnings("PMD.CommentRequired")
public class GlobalTagEvent extends TagEvent {

    public GlobalTagEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        staticness = true;
        persistent = true;
    }

    @Override
    protected Void execute(final String playerID) {
        final GlobalData globalData = BetonQuest.getInstance().getGlobalData();
        if (add) {
            for (final String tag : tags) {
                globalData.addTag(tag);
            }
        } else {
            for (final String tag : tags) {
                globalData.removeTag(tag);
            }
        }
        return null;
    }
}
