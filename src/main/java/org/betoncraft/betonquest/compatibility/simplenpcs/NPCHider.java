package org.betoncraft.betonquest.compatibility.simplenpcs;

import com.github.juliarn.npc.NPC;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.config.Config;
import org.betoncraft.betonquest.config.ConfigPackage;
import org.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import org.betoncraft.betonquest.id.ConditionID;
import org.betoncraft.betonquest.utils.LogUtils;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@SuppressWarnings("PMD.CommentRequired")
public final class NPCHider extends BukkitRunnable implements Listener {

    private static NPCHider instance;

    private final Map<Integer, Set<ConditionID>> npcs;

    private NPCHider() {
        super();
        npcs = new HashMap<>();
        final int updateInterval = BetonQuest.getInstance().getConfig().getInt("npc_hider_check_interval", 5 * 20);
        loadFromConfig();
        runTaskTimer(BetonQuest.getInstance(), 0, updateInterval);
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    /**
     * Starts (or restarts) the NPCHider. It loads the current configuration for hidden NPCs
     */
    public static void start() {
        if (instance != null) {
            instance.stop();
        }
        instance = new NPCHider();
    }

    /**
     * @return the currently used NPCHider instance
     */
    public static NPCHider getInstance() {
        return instance;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void loadFromConfig() {
        for (final ConfigPackage cfgPackage : Config.getPackages().values()) {
            final FileConfiguration custom = cfgPackage.getCustom().getConfig();
            if (custom == null) {
                continue;
            }
            final ConfigurationSection section = custom.getConfigurationSection("hide_npcs");
            if (section == null) {
                continue;
            }
            npcs:
            for (final String npcIds : section.getKeys(false)) {
                final int npcId;
                try {
                    npcId = Integer.parseInt(npcIds);
                } catch (final NumberFormatException e) {
                    LogUtils.getLogger().log(Level.WARNING, "NPC ID '" + npcIds + "' is not a valid number, in custom.yml hide_npcs");
                    LogUtils.logThrowable(e);
                    continue npcs;
                }
                final Set<ConditionID> conditions = new HashSet<>();
                final String conditionsString = section.getString(npcIds);

                for (final String condition : conditionsString.split(",")) {
                    try {
                        conditions.add(new ConditionID(cfgPackage, condition));
                    } catch (final ObjectNotFoundException e) {
                        LogUtils.getLogger().log(Level.WARNING, "Condition '" + condition +
                                "' does not exist, in custom.yml hide_npcs with ID " + npcIds);
                        LogUtils.logThrowable(e);
                        continue npcs;
                    }
                }

                if (npcs.containsKey(npcId)) {
                    npcs.get(npcId).addAll(conditions);
                } else {
                    npcs.put(npcId, conditions);
                }
            }
        }

    }

    @Override
    public void run() {
        applyVisibility();
    }

    /**
     * Stops the NPCHider, cleaning up all listeners, runnables etc.
     */
    public void stop() {
        cancel();
        HandlerList.unregisterAll(this);
    }

    /**
     * Updates the visibility of the specified NPC for this player.
     *
     * @param player the player
     * @param npcID  ID of the NPC
     */
    public void applyVisibility(final Player player, final Integer npcID) {
        final NPC npc = BetonQuest.simpleNPCs().getNPC(npcID);
        if (npc == null) {
            if (!BetonQuest.simpleNPCs().isFinishedLoading()) return;
            LogUtils.getLogger().log(Level.WARNING, "NPCHider could not update visibility for npc " + npcID + ": No npc with this id found!");
            return;
        }
        final Set<ConditionID> conditions = npcs.get(npcID);
        if (conditions == null || conditions.isEmpty() || !BetonQuest.conditions(PlayerConverter.getID(player), conditions)) {
            // Show NPC
            BetonQuest.simpleNPCs().showNPCToPlayer(npc, player);
        } else {
            // Hide NPC
            BetonQuest.simpleNPCs().hideNPCFromPlayer(npc, player);
        }
    }

    /**
     * Updates the visibility of all NPCs for this player.
     *
     * @param player the player
     */
    public void applyVisibility(final Player player) {
        for (final Integer npcID : npcs.keySet()) {
            applyVisibility(player, npcID);
        }
    }

    /**
     * Updates the visibility of this NPC for all players.
     *
     * @param npcID ID of the NPC
     */
    public void applyVisibility(final NPC npcID) {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            applyVisibility(p, BetonQuest.simpleNPCs().getID(npcID));
        }
    }

    /**
     * Updates the visibility of all NPCs for all players.
     */
    public void applyVisibility() {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            for (final Integer npcID : npcs.keySet()) {
                applyVisibility(p, npcID);
            }
        }
    }

    /**
     * Checks whenever the NPC is visible to the player.
     *
     * @param player the player
     * @param npc    ID of the NPC
     * @return true if the NPC is visible to that player, false otherwise
     */
    public boolean isInvisible(final Player player, final NPC npc) {
        return !npc.isShownFor(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        applyVisibility(event.getPlayer());
    }
}
