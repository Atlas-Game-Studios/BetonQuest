package org.betonquest.betonquest.compatibility.simplenpcs;

import com.github.arnhav.SimpleNPCs;
import com.github.arnhav.objects.SNPC;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Variable;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.bukkit.Location;

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
    public String getValue(final Profile profile) {
        final SNPC npc = SimpleNPCs.npcManager().getNPC(npcId);
        if (npc == null) {
            return "";
        }

        switch (key) {
            case NAME:
            case FULL_NAME:
                return npc.getProfile().name();
            case LOCATION:
                final Location loc = npc.getBukkitLocation();
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
