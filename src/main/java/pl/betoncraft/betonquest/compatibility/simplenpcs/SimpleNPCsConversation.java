package pl.betoncraft.betonquest.compatibility.simplenpcs;

import com.github.juliarn.npc.NPC;
import org.bukkit.Location;
import pl.betoncraft.betonquest.conversation.Conversation;

/**
 * Represents a conversation with NPC
 */
@SuppressWarnings("PMD.CommentRequired")
public class SimpleNPCsConversation extends Conversation {

    private final NPC npc;

    public SimpleNPCsConversation(final String playerID, final String conversationID, final Location location, final NPC npc) {
        super(playerID, conversationID, location);
        this.npc = npc;
    }

    /**
     * This will return the NPC associated with this conversation only after the
     * coversation is created (all player options are listed and ready to
     * receive player input)
     *
     * @return the NPC or null if it's too early
     */
    public NPC getNPC() {
        return npc;
    }

}
