package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;

/**
 * Checks if the player has specified condition.
 */
@SuppressWarnings("PMD.CommentRequired")
public class AdvancementCondition extends Condition {

    private final Advancement advancement;

    @SuppressWarnings("deprecation")
    public AdvancementCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        final String advancementString = instruction.next();
        if (!advancementString.contains(":")) {
            throw new InstructionParseException("The advancement '" + advancementString + "' is missing a namespace!");
        }
        final String[] split = advancementString.split(":");
        advancement = Bukkit.getServer().getAdvancement(new NamespacedKey(split[0], split[1]));
        if (advancement == null) {
            throw new InstructionParseException("No such advancement: " + advancementString);
        }
    }

    @Override
    protected Boolean execute(final String playerID) {
        final AdvancementProgress progress = PlayerConverter.getPlayer(playerID).getAdvancementProgress(advancement);
        return progress.isDone();
    }

}
