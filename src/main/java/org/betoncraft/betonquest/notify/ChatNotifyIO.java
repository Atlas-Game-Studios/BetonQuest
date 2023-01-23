package org.betoncraft.betonquest.notify;

import org.betoncraft.betonquest.conversation.Conversation;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

import java.util.Map;

@SuppressWarnings("PMD.CommentRequired")
public class ChatNotifyIO extends NotifyIO {

    public ChatNotifyIO(final Map<String, String> data) throws InstructionParseException {
        super(data);
    }

    @Override
    protected void notifyPlayer(final String message, final Player player) {
        final Conversation conversation = Conversation.getConversation(PlayerConverter.getID(player));
        if (conversation == null || conversation.getInterceptor() == null) {
            player.sendMessage(message);
        } else {
            conversation.getInterceptor().sendMessage(message);
        }
    }
}
