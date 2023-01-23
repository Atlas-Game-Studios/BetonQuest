package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.item.QuestItem;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Holding item in hand condition
 */
@SuppressWarnings("PMD.CommentRequired")
public class HandCondition extends Condition {

    private final QuestItem questItem;
    private final boolean offhand;

    public HandCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        questItem = new QuestItem(instruction.getItem());
        offhand = instruction.hasArgument("offhand");
    }

    @Override
    protected Boolean execute(final String playerID) {
        final PlayerInventory inv = PlayerConverter.getPlayer(playerID).getInventory();
        final ItemStack item = offhand ? inv.getItemInOffHand() : inv.getItemInMainHand();

        return questItem.compare(item);
    }

}
