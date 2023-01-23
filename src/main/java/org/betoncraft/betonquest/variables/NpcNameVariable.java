package org.betoncraft.betonquest.variables;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Variable;
import org.betoncraft.betonquest.conversation.Conversation;

/**
 * This variable resolves into the name of the NPC.
 */
@SuppressWarnings("PMD.CommentRequired")
public class NpcNameVariable extends Variable {

    public NpcNameVariable(final Instruction instruction) {
        super(instruction);
    }

    @Override
    public String getValue(final String playerID) {
        final Conversation conv = Conversation.getConversation(playerID);
        if (conv == null) {
            return "";
        }
        return conv.getData().getQuester(BetonQuest.getInstance().getPlayerData(playerID).getLanguage());
    }

}
