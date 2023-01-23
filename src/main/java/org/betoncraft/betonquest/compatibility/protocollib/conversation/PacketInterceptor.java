package org.betoncraft.betonquest.compatibility.protocollib.conversation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.AdventureComponentConverter;
import com.comphenix.protocol.wrappers.ComponentConverter;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.compatibility.protocollib.wrappers.WrapperPlayServerChat;
import org.betoncraft.betonquest.conversation.Conversation;
import org.betoncraft.betonquest.conversation.Interceptor;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a packet interceptor to get all chat packets to player
 */
@SuppressWarnings("PMD.CommentRequired")
public class PacketInterceptor implements Interceptor, Listener {

    /**
     * A prefix that marks messages to be ignored by this interceptor.
     * To be invisible if the interceptor was closed before the message was sent the tag is a color code.
     * The actual tags colors are the hex-representation of the ASCII representing the string '_bq_'.
     */
    private static final String MESSAGE_PASSTHROUGH_TAG = "§5§f§6§2§7§1§5§f";

    protected final Conversation conv;
    protected final Player player;
    private final List<WrapperPlayServerChat> messages = new ArrayList<>();
    private final PacketAdapter packetAdapter;

    public PacketInterceptor(final Conversation conv, final String playerID) {
        this.conv = conv;
        this.player = PlayerConverter.getPlayer(playerID);

        // Intercept Packets
        packetAdapter = new PacketAdapter(BetonQuest.getInstance(), ListenerPriority.HIGHEST, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                if (event.getPlayer() != player) return;

                if (!event.getPacketType().equals(PacketType.Play.Server.CHAT)) return;
                final WrapperPlayServerChat packet = new WrapperPlayServerChat(event.getPacket());
                Component message = packet.getMessage();
                WrappedChatComponent wrappedChatComponent = AdventureComponentConverter.fromComponent(message);
                final BaseComponent[] components = ComponentConverter.fromWrapper(wrappedChatComponent);
                if (components != null && components.length > 0 && ((TextComponent) components[0]).getText().contains(MESSAGE_PASSTHROUGH_TAG))
                    return;

                // Else save message to replay later
                event.setCancelled(true);
                messages.add(packet);
            }
        };

        final AsyncListenerHandler handler = ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(packetAdapter);
        handler.start();
    }

    /**
     * Send message, bypassing Interceptor
     */
    @Override
    public void sendMessage(final String message) {
        sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(final BaseComponent... message) {
        final BaseComponent[] components = ArrayUtils.addAll(new TextComponent[]{new TextComponent(MESSAGE_PASSTHROUGH_TAG)}, message);
        player.spigot().sendMessage(components);
    }


    @Override
    public void end() {
        // Stop Listening for Packets
        ProtocolLibrary.getProtocolManager().getAsynchronousManager().unregisterAsyncHandler(packetAdapter);

        //Send all messages to player
        for (final WrapperPlayServerChat message : messages) {
            message.sendPacket(player);
        }
    }
}
