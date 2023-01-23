package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

/**
 * Requires the player to be in a specified biome
 */
@SuppressWarnings("PMD.CommentRequired")
public class BiomeCondition extends Condition {

    private final Biome biome;

    public BiomeCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        biome = instruction.getEnum(Biome.class);
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final Player player = PlayerConverter.getPlayer(playerID);
        return player.getLocation().getBlock().getBiome() == this.biome;
    }
}
