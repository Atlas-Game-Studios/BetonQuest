package org.betonquest.betonquest.compatibility.simplenpcs;

import com.github.arnhav.objects.SNPC;
import com.github.juliarn.npc.NPC;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.conversation.Conversation;
import org.bukkit.Location;

/**
 * Represents a conversation with NPC
 */
@SuppressWarnings("PMD.CommentRequired")
public class SimpleNPCsConversation extends Conversation {

    private final SNPC npc;

    public SimpleNPCsConversation(final OnlineProfile onlineProfile, final String conversationID, final Location location, final SNPC npc) {
        super(onlineProfile, conversationID, location);
        this.npc = npc;
    }

    /**
     * This will return the NPC associated with this conversation only after the
     * coversation is created (all player options are listed and ready to
     * receive player input)
     *
     * @return the NPC or null if it's too early
     */
    public SNPC getNPC() {
        return npc;
    }

}
