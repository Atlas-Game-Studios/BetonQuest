package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Checks if the player has required amount of empty slots in his inventory
 */
@SuppressWarnings("PMD.CommentRequired")
public class EmptySlotsCondition extends Condition {

    private final VariableNumber needed;
    private final boolean equal;

    public EmptySlotsCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        needed = instruction.getVarNum();
        equal = instruction.hasArgument("equal");
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final Player player = PlayerConverter.getPlayer(playerID);
        final ItemStack[] items = player.getInventory().getStorageContents();

        int empty = 0;
        for (final ItemStack item : items) {
            if (item == null) {
                empty++;
            }
        }
        return equal ? empty == needed.getInt(playerID) : empty >= needed.getInt(playerID);
    }

}
