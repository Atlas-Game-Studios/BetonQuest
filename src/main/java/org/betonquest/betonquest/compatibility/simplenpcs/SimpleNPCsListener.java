package org.betonquest.betonquest.compatibility.simplenpcs;

import com.github.arnhav.api.NPCClickEvent;
import com.github.arnhav.api.NPCLeftClickEvent;
import com.github.arnhav.api.NPCRightClickEvent;
import com.github.arnhav.objects.SNPC;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.conversation.CombatTagger;
import org.betonquest.betonquest.exceptions.ObjectNotFoundException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.id.ConversationID;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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
    /**
     * The section in which the assignments from NPCs to conversations are stored.
     */
    private static final String NPC_SECTION = "npcs";

    /**
     * Custom {@link BetonQuestLogger} instance for this class.
     */
    private final BetonQuestLogger log;

    /**
     * Stores the last time the player interacted with an NPC.
     */
    private final Map<UUID, Long> npcInteractionLimiter = new HashMap<>();

    /**
     * The {@link BetonQuestLoggerFactory} to use for creating {@link BetonQuestLogger} instances.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Stores the conversations assigned to NPCs via the configuration.
     * The key could either be an NPC's name or its ID, depending on the configuration.
     */
    private final Map<String, ConversationID> assignedConversations = new HashMap<>();

    /**
     * A listener for right-clicking a Citizens NPC.
     */
    private RightClickListener rightClick;

    /**
     * A listener for left-clicking a Citizens NPC.
     */
    private LeftClickListener leftClick;

    /**
     * The minimum time between two interactions with an NPC.
     */
    private int interactionLimit;

    /**
     * Initializes the listener
     */
    public SimpleNPCsListener(final BetonQuestLoggerFactory loggerFactory, final BetonQuestLogger log) {
        this.loggerFactory = loggerFactory;
        this.log = log;
        reload();
    }

    /**
     * Reloads the listeners.
     */
    public final void reload() {
        if (rightClick != null) {
            HandlerList.unregisterAll(rightClick);
        }
        if (leftClick != null) {
            HandlerList.unregisterAll(leftClick);
        }

        assignedConversations.clear();
        loadNPCConversationAssignments();

        final BetonQuest plugin = BetonQuest.getInstance();

        rightClick = new RightClickListener();
        Bukkit.getPluginManager().registerEvents(rightClick, plugin);

        if (plugin.getPluginConfig().getBoolean("acceptNPCLeftClick")) {
            leftClick = new LeftClickListener();
            Bukkit.getPluginManager().registerEvents(leftClick, plugin);
        }
        interactionLimit = plugin.getPluginConfig().getInt("npcInteractionLimit", 500);
    }

    /**
     * The logic that determines if an NPC interaction starts a conversation.
     *
     * @param event the event for the NPC interaction
     */
    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    private void interactLogic(final NPCClickEvent event) {
        if (!event.getPlayer().hasPermission("betonquest.conversation")) {
            return;
        }
        final UUID playerUUID = event.getPlayer().getUniqueId();

        final Long lastClick = npcInteractionLimiter.get(playerUUID);
        final long currentClick = new Date().getTime();
        if (lastClick != null && lastClick + interactionLimit >= currentClick) {
            return;
        }
        npcInteractionLimiter.put(playerUUID, currentClick);

        final SNPC npc = event.getNpc();

        final OnlineProfile onlineProfile = PlayerConverter.getID(event.getPlayer());
        if (CombatTagger.isTagged(onlineProfile)) {
            try {
                Config.sendNotify(null, onlineProfile, "busy", "busy,error");
            } catch (final QuestRuntimeException e) {
                log.warn("The notify system was unable to play a sound for the 'busy' category. Error was: '" + e.getMessage() + "'", e);
            }
            return;
        }

        final String npcId = String.valueOf(npc.getId());
        final String npcName = npc.getName().getFullName();

        final boolean npcsByName = "true".equalsIgnoreCase(Config.getString("config.citizens_npcs_by_name"));

        final String selector = npcsByName ? npcName : npcId;
        final ConversationID conversationID = assignedConversations.get(selector);

        if (conversationID == null) {
            log.debug("Player '" + event.getPlayer().getName() + "' clicked NPC '" + npcId + "' but there is no conversation assigned to it.");
        } else {
            event.setCancelled(true);
            new SimpleNPCsConversation(loggerFactory.create(SimpleNPCsConversation.class), onlineProfile,
                    conversationID, npc.getBukkitLocation(), event.getNpc());
        }
    }

    private void loadNPCConversationAssignments() {
        for (final Map.Entry<String, QuestPackage> entry : Config.getPackages().entrySet()) {
            final QuestPackage pack = entry.getValue();

            final ConfigurationSection assignments = pack.getConfig().getConfigurationSection(NPC_SECTION);

            if (assignments == null) {
                continue;
            }
            for (final Map.Entry<String, Object> assignment : assignments.getValues(false).entrySet()) {
                final ConversationID conversationID;
                final String npcID = assignment.getKey();
                final String conversationIDPath = assignment.getValue().toString();
                try {
                    conversationID = new ConversationID(pack, conversationIDPath);
                    assignedConversations.put(npcID, conversationID);
                } catch (final ObjectNotFoundException e) {
                    log.warn("Conversation '" + conversationIDPath + "' assigned to NPC '" + npcID + "' in package '" + pack.getQuestPath() + "' does not exist.", e);
                }
            }
        }
    }

    /**
     * A listener for right-clicking a Citizens NPC.
     */
    @SuppressWarnings("PMD.CommentRequired")
    private class RightClickListener implements Listener {

        public RightClickListener() {
        }

        @EventHandler(ignoreCancelled = true)
        public void onNPCClick(final NPCRightClickEvent event) {
            interactLogic(event);
        }
    }

    /**
     * A listener for left-clicking a Citizens NPC.
     */
    @SuppressWarnings("PMD.CommentRequired")
    private class LeftClickListener implements Listener {

        public LeftClickListener() {
        }

        @EventHandler(ignoreCancelled = true)
        public void onNPCClick(final NPCLeftClickEvent event) {
            interactLogic(event);
        }
    }
}
