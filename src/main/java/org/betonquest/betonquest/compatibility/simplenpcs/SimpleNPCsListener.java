package org.betonquest.betonquest.compatibility.simplenpcs;

import com.github.arnhav.api.NPCClickEvent;
import com.github.arnhav.api.NPCLeftClickEvent;
import com.github.arnhav.api.NPCRightClickEvent;
import com.github.arnhav.objects.SNPC;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestLogger;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.conversation.CombatTagger;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Starts new conversations with NPCs
 */
@SuppressWarnings("PMD.CommentRequired")
public class SimpleNPCsListener implements Listener {

    private static final BetonQuestLogger LOG = BetonQuestLogger.create();

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

        final OnlineProfile onlineProfile = PlayerConverter.getID(event.getPlayer());
        if (CombatTagger.isTagged(onlineProfile)) {
            try {
                Config.sendNotify(null, onlineProfile, "busy", "busy,error");
            } catch (final QuestRuntimeException exception) {
                LOG.warn("The notify system was unable to play a sound for the 'busy' category. Error was: '" + exception.getMessage() + "'");
            }
            return;
        }
        SNPC snpc = event.getNpc();
        final String npcId = String.valueOf(snpc.getId());
        String assignment = Config.getNpc(npcId);
        if ("true".equalsIgnoreCase(Config.getString("config.citizens_npcs_by_name")) && assignment == null) {
            assignment = Config.getNpc(snpc.getProfile().getName());
        }
        if (assignment != null) {
            event.setCancelled(true);
            new SimpleNPCsConversation(onlineProfile, assignment, snpc.getLocation(), snpc);
        }
    }
}
