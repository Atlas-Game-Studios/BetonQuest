package org.betoncraft.betonquest.compatibility.quests;

import me.blackvein.quests.CustomReward;
import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import org.betoncraft.betonquest.id.EventID;
import org.betoncraft.betonquest.utils.LogUtils;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.logging.Level;

/**
 * Fires a BetonQuest event as a quest reward.
 */
@SuppressWarnings("PMD.CommentRequired")
public class EventReward extends CustomReward {

    public EventReward() {
        super();
        setName("BetonQuest event");
        setAuthor("Co0sh");
        setRewardName("Event");
        addStringPrompt("Event", "Specify BetonQuest event name (with the package, like: package.event)", null);
    }

    @Override
    public void giveReward(final Player player, final Map<String, Object> dataMap) {
        final String string = dataMap.get("Event").toString();
        try {
            final String playerID = PlayerConverter.getID(player);
            final EventID event = new EventID(null, string);
            BetonQuest.event(playerID, event);
        } catch (ObjectNotFoundException e) {
            LogUtils.getLogger().log(Level.WARNING, "Error while running quest reward - BetonQuest event '" + string + "' not found: " + e.getMessage());
            LogUtils.logThrowable(e);
        }
    }

}
