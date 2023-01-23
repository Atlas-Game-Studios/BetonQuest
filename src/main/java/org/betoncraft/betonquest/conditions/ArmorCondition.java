package org.betoncraft.betonquest.conditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.item.QuestItem;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.inventory.ItemStack;

/**
 * Player has to wear this item as an armor
 */
@SuppressWarnings("PMD.CommentRequired")
public class ArmorCondition extends Condition {

    private final QuestItem item;

    public ArmorCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        item = new QuestItem(instruction.getItem());
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    protected Boolean execute(final String playerID) {
        for (final ItemStack armor : PlayerConverter.getPlayer(playerID).getEquipment().getArmorContents()) {
            if (item != null && item.compare(armor)) {
                return true;
            }
        }
        return false;
    }

}
