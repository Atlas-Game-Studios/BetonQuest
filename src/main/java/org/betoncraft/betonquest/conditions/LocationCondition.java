package org.betoncraft.betonquest.conditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.betoncraft.betonquest.utils.location.CompoundLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Requires the player to be in specified distance from a location
 */
@SuppressWarnings("PMD.CommentRequired")
public class LocationCondition extends Condition {

    private final CompoundLocation loc;
    private final VariableNumber range;

    public LocationCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        loc = instruction.getLocation();
        range = instruction.getVarNum();
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final Location location = loc.getLocation(playerID);
        final Player player = PlayerConverter.getPlayer(playerID);
        if (!location.getWorld().equals(player.getWorld())) {
            return false;
        }
        final double pRange = range.getDouble(playerID);
        return player.getLocation().distanceSquared(location) <= pRange * pRange;
    }

}
