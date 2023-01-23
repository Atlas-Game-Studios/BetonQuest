package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.database.Connector;
import org.betoncraft.betonquest.database.PlayerData;
import org.betoncraft.betonquest.database.Saver;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.betoncraft.betonquest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Modifies player's points
 */
@SuppressWarnings("PMD.CommentRequired")
public class DeletePointEvent extends QuestEvent {

    protected final String category;

    public DeletePointEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        persistent = true;
        staticness = true;
        category = Utils.addPackage(instruction.getPackage(), instruction.next());
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        if (playerID == null) {
            for (final Player p : Bukkit.getOnlinePlayers()) {
                final PlayerData playerData = BetonQuest.getInstance().getPlayerData(PlayerConverter.getID(p));
                playerData.removePointsCategory(category);
            }
            BetonQuest.getInstance().getSaver().add(new Saver.Record(Connector.UpdateType.REMOVE_ALL_POINTS, category));
        } else if (PlayerConverter.getPlayer(playerID) == null) {
            final PlayerData playerData = new PlayerData(playerID);
            playerData.removePointsCategory(category);
        } else {
            final PlayerData playerData = BetonQuest.getInstance().getPlayerData(playerID);
            playerData.removePointsCategory(category);
        }
        return null;
    }
}
