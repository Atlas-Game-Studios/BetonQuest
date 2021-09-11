package pl.betoncraft.betonquest.compatibility.simplenpcs;

import com.github.juliarn.npc.NPC;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.bukkit.Location;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.api.Variable;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;

import java.util.Locale;

/**
 * Provides information about a citizen npc.
 * <p>
 * Format:
 * {@code %citizen.<id>.<type>%}
 * <p>
 * Types:
 * * name - Return citizen name
 * * full_name - Full Citizen name
 * * location  - Return citizen location. x;y;z;world;yaw;pitch
 */
@SuppressWarnings("PMD.CommentRequired")
public class SimpleNPCsVariable extends Variable {

    private final int npcId;
    private final TYPE key;

    public SimpleNPCsVariable(final Instruction instruction) throws InstructionParseException {
        super(instruction);

        npcId = instruction.getInt();
        try {
            key = TYPE.valueOf(instruction.next().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            throw new InstructionParseException("Invalid Type: " + instruction.current(), e);
        }
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public String getValue(final String playerID) {
        final NPC npc = BetonQuest.simpleNPCs().getNPC(npcId);
        if (npc == null) {
            return "";
        }

        switch (key) {
            case NAME:
            case FULL_NAME:
                return npc.getProfile().getName();
            case LOCATION:
                final Location loc = npc.getLocation();
                return String.format("%.2f;%.2f;%.2f;%s;%.2f;%.2f",
                        loc.getX(),
                        loc.getY(),
                        loc.getZ(),
                        loc.getWorld().getName(),
                        loc.getYaw(),
                        loc.getPitch());
        }
        return "";
    }

    private enum TYPE {
        NAME,
        FULL_NAME,
        LOCATION
    }

}
