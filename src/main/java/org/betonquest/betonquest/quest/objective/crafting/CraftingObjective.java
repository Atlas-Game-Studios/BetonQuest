package org.betonquest.betonquest.quest.objective.crafting;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.Item;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.betonquest.betonquest.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Player has to craft specified amount of items.
 */
public class CraftingObjective extends CountingObjective implements Listener {
    /**
     * Custom logger for this class.
     */
    private final BetonQuestLogger log;

    /**
     * The item to be crafted.
     */
    private final Item item;

    /**
     * Constructor for the CraftingObjective.
     *
     * @param instruction  the instruction that created this objective
     * @param targetAmount the target amount of items to be crafted
     * @param log          the custom logger instance for this objective
     * @param item         the item to be crafted
     * @throws QuestException if there is an error in the instruction
     */
    public CraftingObjective(final Instruction instruction, final VariableNumber targetAmount, final BetonQuestLogger log,
                             final Item item) throws QuestException {
        super(instruction, targetAmount, "items_to_craft");
        this.log = log;
        this.item = item;
    }

    private static int calculateCraftAmount(final CraftItemEvent event) {
        final ItemStack result = event.getInventory().getResult();
        final PlayerInventory inventory = event.getWhoClicked().getInventory();
        final ItemStack[] ingredients = event.getInventory().getMatrix();
        return switch (event.getClick()) {
            case SHIFT_LEFT, SHIFT_RIGHT -> InventoryUtils.calculateShiftCraftAmount(result, inventory, ingredients);
            case CONTROL_DROP -> InventoryUtils.calculateMaximumCraftAmount(result, ingredients);
            case NUMBER_KEY ->
                    InventoryUtils.calculateSwapCraftAmount(result, inventory.getItem(event.getHotbarButton()));
            case SWAP_OFFHAND -> InventoryUtils.calculateSwapCraftAmount(result, inventory.getItemInOffHand());
            case DROP -> InventoryUtils.calculateDropCraftAmount(result, event.getCursor());
            case LEFT, RIGHT -> InventoryUtils.calculateSimpleCraftAmount(result, event.getCursor());
            default -> 0;
        };
    }

    /**
     * Checks if the player has crafted the item.
     *
     * @param event the CraftItemEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrafting(final CraftItemEvent event) {
        if (event.getWhoClicked() instanceof final Player player) {
            final OnlineProfile onlineProfile = profileProvider.getProfile(player);
            try {

                if (containsPlayer(onlineProfile) && item.matches(event.getInventory().getResult()) && checkConditions(onlineProfile)) {
                    getCountingData(onlineProfile).progress(calculateCraftAmount(event));
                    completeIfDoneOrNotify(onlineProfile);
                }
            } catch (final QuestException e) {
                log.warn(instruction.getPackage(), "Exception while processing Crafting Objective: " + e.getMessage(), e);
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
