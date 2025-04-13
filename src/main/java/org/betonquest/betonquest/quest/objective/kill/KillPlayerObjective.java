package org.betonquest.betonquest.quest.objective.kill;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.id.ConditionID;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Requires the player to kill a target player.
 */
public class KillPlayerObjective extends CountingObjective implements Listener {
    /**
     * The name of the victim to kill.
     */
    @Nullable
    private final String name;

    /**
     * The conditions of the victim that must be met for the objective to count.
     */
    private final List<ConditionID> required;

    /**
     * Constructor for the KillPlayerObjective.
     *
     * @param instruction  the instruction that created this objective
     * @param targetAmount the amount of players to kill
     * @param name         the name of the player to kill, or null for any player
     * @param required     the conditions of the victim that must be met for the objective to count
     * @throws QuestException if there is an error in the instruction
     */
    public KillPlayerObjective(final Instruction instruction, final VariableNumber targetAmount,
                               @Nullable final String name, final List<ConditionID> required) throws QuestException {
        super(instruction, "players_to_kill");
        this.targetAmount = targetAmount;
        this.name = name;
        this.required = required;
    }

    /**
     * Check if the player is the killer of the victim.
     *
     * @param event the PlayerDeathEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onKill(final PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            final OnlineProfile victim = profileProvider.getProfile(event.getEntity());
            final OnlineProfile killer = profileProvider.getProfile(event.getEntity().getKiller());

            if (containsPlayer(killer)
                    && (name == null || event.getEntity().getName().equalsIgnoreCase(name))
                    && BetonQuest.getInstance().getQuestTypeAPI().conditions(victim, required)
                    && checkConditions(killer)) {

                getCountingData(killer).progress();
                completeIfDoneOrNotify(killer);
            }
        }
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }
}
