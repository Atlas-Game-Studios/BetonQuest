package org.betonquest.betonquest.quest.objective.pickup;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.Item;
import org.betonquest.betonquest.instruction.variable.Variable;
import org.betonquest.betonquest.instruction.variable.VariableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an objective that is completed when a player picks up a specific item.
 */
public class PickupObjective extends CountingObjective implements Listener {
    /**
     * Custom logger for this class.
     */
    private final BetonQuestLogger log;

    /**
     * The target amount of items to be picked up.
     */
    private final VariableList<Item> pickupItems;

    /**
     * Constructor for the PickupObjective.
     *
     * @param instruction  the instruction that created this objective
     * @param targetAmount the target amount of items to be picked up
     * @param log          the logger for this objective
     * @param pickupItems  the items to be picked up
     * @throws QuestException if there is an error in the instruction
     */
    public PickupObjective(final Instruction instruction, final Variable<Number> targetAmount,
                           final BetonQuestLogger log, final VariableList<Item> pickupItems) throws QuestException {
        super(instruction, targetAmount, "items_to_pickup");
        this.log = log;
        this.pickupItems = pickupItems;
    }

    /**
     * Handles when the player picks up an item.
     *
     * @param event The event object.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPickup(final EntityPickupItemEvent event) {
        if (event.getEntity() instanceof final Player player) {
            final OnlineProfile onlineProfile = profileProvider.getProfile(player);
            try {
                if (containsPlayer(onlineProfile) && isValidItem(onlineProfile, event.getItem().getItemStack()) && checkConditions(onlineProfile)) {
                    final ItemStack pickupItem = event.getItem().getItemStack();
                    getCountingData(onlineProfile).progress(pickupItem.getAmount());
                    completeIfDoneOrNotify(onlineProfile);
                }
            } catch (final QuestException e) {
                log.warn(instruction.getPackage(), "Exception while processing Pickup Objective: " + e.getMessage(), e);
            }
        }
    }

    private boolean isValidItem(final OnlineProfile onlineProfile, final ItemStack itemStack) throws QuestException {
        for (final Item item : pickupItems.getValue(onlineProfile)) {
            if (item.matches(itemStack)) {
                return true;
            }
        }
        return false;
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
