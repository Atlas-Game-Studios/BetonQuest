package org.betoncraft.betonquest.conditions;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.BlockSelector;
import org.betoncraft.betonquest.utils.location.CompoundLocation;
import org.bukkit.block.Block;

/**
 * Checks block at specified location against specified {@link BlockSelector}
 */
@SuppressWarnings("PMD.CommentRequired")
public class TestForBlockCondition extends Condition {

    private final CompoundLocation loc;
    private final BlockSelector selector;
    private final boolean exactMatch;

    public TestForBlockCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        staticness = true;
        persistent = true;
        loc = instruction.getLocation();
        selector = instruction.getBlockSelector();
        exactMatch = instruction.hasArgument("exactMatch");
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final Block block = loc.getLocation(playerID).getBlock();
        return selector.match(block, exactMatch);
    }

}
