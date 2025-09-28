package org.betonquest.betonquest.conversation.io;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.conversation.ChatConvIO;
import org.betonquest.betonquest.conversation.Conversation;
import org.betonquest.betonquest.conversation.ConversationColors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Adds tellraw command handling to the SimpleConvIO.
 */
public class TellrawConvIO extends ChatConvIO {

    /**
     * The answer command.
     */
    private static final String BETONQUESTANSWER = "/betonquestanswer ";

    static {
        Bukkit.getPluginManager().registerEvents(new UnknownCommandTellrawListener(), BetonQuest.getInstance());
    }

    /**
     * Option number UUID "hashes".
     */
    protected List<String> hashes;

    /**
     * Creates a new TellrawConvIO instance.
     *
     * @param conv          the conversation this IO is part of
     * @param onlineProfile the online profile of the player participating in the conversation
     * @param colors        the colors used in the conversation
     */
    public TellrawConvIO(final Conversation conv, final OnlineProfile onlineProfile, final ConversationColors colors) {
        super(conv, onlineProfile, colors);
        hashes = new ArrayList<>();
    }

    /**
     * Passes and prints the "clicked" answer to the conversation.
     *
     * @param event the preprocess event
     */
    @EventHandler(ignoreCancelled = true)
    public void onClickAnswer(final PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        final Player player = conn.getPlayer();
        if (!player.equals(onlineProfile.getPlayer())) return;
        final Key key = event.getIdentifier();
        final String hash = key.value();
        for (int j = 1; j <= hashes.size(); j++) {
            if (hash.equals(hashes.get(j - 1))) {
                conv.sendMessage(colors.getAnswer().append(colors.getPlayer().append(Component.text(onlineProfile.getPlayer().getName())))
                        .append(Component.text(": ")).append(options.get(j)));
                conv.passPlayerAnswer(j);
                return;
            }
        }
    }

    @Override
    public void display() {
        super.display();
        displayText();
    }

    /**
     * Displays all player answers.
     */
    protected void displayText() {
        for (int j = 1; j <= options.size(); j++) {
            final TextComponent message = Component.empty()
                    .clickEvent(ClickEvent.custom(Key.key("bq" + ":" + hashes.get(j - 1)), BinaryTagHolder.binaryTagHolder("bqanswer")))
                    .append(colors.getOption().append(colors.getNumber().append(Component.text(j)).append(Component.text(". ")))
                            .append(options.get(j)));

            conv.sendMessage(message);
        }
    }

    @Override
    public void addPlayerOption(final Component option, final ConfigurationSection properties) {
        super.addPlayerOption(option, properties);
        hashes.add(UUID.randomUUID().toString());
    }

    @Override
    public void clear() {
        super.clear();
        hashes.clear();
    }

    /**
     * Command Listener to cancel all answer command processing.
     */
    public static class UnknownCommandTellrawListener implements Listener {

        /**
         * The empty default constructor.
         */
        public UnknownCommandTellrawListener() {
        }

        /**
         * Cancels the answer command processing.
         *
         * @param event the preprocess event
         */
        @EventHandler(priority = EventPriority.HIGH)
        public void onCommand(final PlayerCommandPreprocessEvent event) {
            if (event.getMessage().toLowerCase(Locale.ROOT).startsWith(BETONQUESTANSWER)) {
                event.setCancelled(true);
            }
        }
    }
}
