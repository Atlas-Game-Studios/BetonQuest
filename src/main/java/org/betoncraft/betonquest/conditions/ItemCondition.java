package org.betoncraft.betonquest.conditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.Instruction.Item;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Requires the player to have specified amount of items in the inventory
 */
@SuppressWarnings("PMD.CommentRequired")
public class ItemCondition extends Condition {

    private final Item[] questItems;

    public ItemCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        questItems = instruction.getItemList();
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        int successfulChecks = 0; // Count of successful checks

        for (final Item questItem : questItems) {
            int counter = 0; // Reset counter for each item
            final int amount = questItem.getAmount().getInt(playerID);

            final ItemStack[] inventoryItems = PlayerConverter.getPlayer(playerID).getInventory().getContents();
            for (final ItemStack item : inventoryItems) {
                if (item == null || !questItem.isItemEqual(item)) {
                    continue;
                }
                counter += item.getAmount();
            }

            final List<ItemStack> backpackItems = BetonQuest.getInstance().getPlayerData(playerID).getBackpack();
            for (final ItemStack item : backpackItems) {
                if (item == null || !questItem.isItemEqual(item)) {
                    continue;
                }
                counter += item.getAmount();
            }
            if (counter >= amount) {
                successfulChecks++;
            }
        }
        return successfulChecks == questItems.length;
    }
}
