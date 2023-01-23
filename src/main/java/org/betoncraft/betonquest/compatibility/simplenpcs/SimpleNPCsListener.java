package org.betoncraft.betonquest.compatibility.simplenpcs;

import com.github.arnhav.api.NPCClickEvent;
import com.github.arnhav.api.NPCLeftClickEvent;
import com.github.arnhav.api.NPCRightClickEvent;
import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.config.Config;
import org.betoncraft.betonquest.conversation.CombatTagger;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.LogUtils;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Starts new conversations with NPCs
 */
@SuppressWarnings("PMD.CommentRequired")
public class SimpleNPCsListener implements Listener {

    private RightClickListener rightClick;
    private LeftClickListener leftClick;

    private final Map<UUID, Long> npcInteractionLimiter = new HashMap<>();
    private int interactionLimit;

    /**
     * Initializes the listener
     */
    public SimpleNPCsListener() {
        reload();
    }

    public final void reload() {
        if (rightClick != null) {
            HandlerList.unregisterAll(rightClick);
        }
        if (leftClick != null) {
            HandlerList.unregisterAll(leftClick);
        }


        final BetonQuest plugin = BetonQuest.getInstance();

        rightClick = new RightClickListener();
        Bukkit.getPluginManager().registerEvents(rightClick, plugin);

        if (plugin.getConfig().getBoolean("acceptNPCLeftClick")) {
            leftClick = new LeftClickListener();
            Bukkit.getPluginManager().registerEvents(leftClick, plugin);
        }
        interactionLimit = plugin.getConfig().getInt("npcInteractionLimit", 500);
    }

    private class RightClickListener implements Listener {

        public RightClickListener() {
        }

        @EventHandler(ignoreCancelled = true)
        public void onNPCClick(final NPCRightClickEvent event) {
            interactLogic(event);
        }
    }

    private class LeftClickListener implements Listener {

        public LeftClickListener() {
        }

        @EventHandler(ignoreCancelled = true)
        public void onNPCClick(final NPCLeftClickEvent event) {
            interactLogic(event);
        }
    }

    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void interactLogic(final NPCClickEvent event) {
        if (!event.getPlayer().hasPermission("betonquest.conversation")) {
            return;
        }
        final Long lastClick = npcInteractionLimiter.get(event.getPlayer().getUniqueId());
        final long currentClick = new Date().getTime();
        if (lastClick != null && lastClick + interactionLimit >= currentClick) {
            return;
        }
        npcInteractionLimiter.put(event.getPlayer().getUniqueId(), currentClick);

        final String playerID = PlayerConverter.getID(event.getPlayer());
        if (CombatTagger.isTagged(playerID)) {
            try {
                Config.sendNotify(null, playerID, "busy", "busy,error");
            } catch (final QuestRuntimeException exception) {
                LogUtils.getLogger().log(Level.WARNING, "The notify system was unable to play a sound for the 'busy' category. Error was: '" + exception.getMessage() + "'");
                LogUtils.logThrowableIgnore(exception);
            }
            return;
        }
        final String npcId = String.valueOf(BetonQuest.simpleNPCs().getID(event.getNPC()));
        String assignment = Config.getNpc(npcId);
        if ("true".equalsIgnoreCase(Config.getString("config.citizens_npcs_by_name")) && assignment == null) {
            assignment = Config.getNpc(event.getNPC().getProfile().getName());
        }
        if (assignment != null) {
            event.setCancelled(true);
            new SimpleNPCsConversation(playerID, assignment, event.getNPC().getLocation(), event.getNPC());
        }
    }
}
