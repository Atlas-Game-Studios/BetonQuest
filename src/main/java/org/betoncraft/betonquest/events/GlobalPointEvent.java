package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.Point;
import org.betoncraft.betonquest.database.GlobalData;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;

/**
 * Modifies global Points
 */
@SuppressWarnings("PMD.CommentRequired")
public class GlobalPointEvent extends PointEvent {

    public GlobalPointEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        staticness = true;
        persistent = true;
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        final GlobalData globalData = BetonQuest.getInstance().getGlobalData();
        addPoints(playerID, globalData);
        return null;
    }

    private void addPoints(final String playerId, final GlobalData globalData) throws QuestRuntimeException {
        if (multi) {
            for (final Point p : globalData.getPoints()) {
                if (p.getCategory().equalsIgnoreCase(category)) {
                    globalData.modifyPoints(category,
                            (int) Math.floor(p.getCount() * count.getDouble(playerId) - p.getCount()));
                }
            }
        } else {
            globalData.modifyPoints(category, count.getInt(playerId));
        }
    }
}
