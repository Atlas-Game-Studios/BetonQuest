package org.betonquest.betonquest.mc_1_21_8.conversation.io;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.ClickEvent;
import org.betonquest.betonquest.conversation.io.TellrawConvIO;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Atlas: answers tellraw conversations with custom clicks instead of run_command clicks,
 * which open a confirmation screen on 1.21.6+ clients.
 */
public class CustomClickAnswerListener implements Listener {

    /**
     * Namespace of the custom click keys used for answers.
     */
    private static final String NAMESPACE = "bq";

    /**
     * Create a new listener and switch the tellraw answers to custom clicks.
     */
    public CustomClickAnswerListener() {
        TellrawConvIO.setAnswerClick(hash -> ClickEvent.custom(Key.key(NAMESPACE, hash), BinaryTagHolder.binaryTagHolder("bqanswer")));
    }

    /**
     * Replays the custom click as the answer command the client used to send,
     * so every tellraw based conversation IO handles it unchanged.
     *
     * @param event the custom click event
     */
    @EventHandler
    public void onCustomClick(final PlayerCustomClickEvent event) {
        final Key key = event.getIdentifier();
        if (NAMESPACE.equals(key.namespace()) && event.getCommonConnection() instanceof final PlayerGameConnection connection) {
            new PlayerCommandPreprocessEvent(connection.getPlayer(), TellrawConvIO.BETONQUESTANSWER + key.value()).callEvent();
        }
    }
}
