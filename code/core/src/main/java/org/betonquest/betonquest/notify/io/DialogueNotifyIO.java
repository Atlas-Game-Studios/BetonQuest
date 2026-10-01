package org.betonquest.betonquest.notify.io;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;
import org.betonquest.betonquest.conversation.menu.MenuConvIOFactory;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;
import org.betonquest.betonquest.kernel.registry.feature.NotifyIORegistry;
import org.betonquest.betonquest.notify.NotifyIO;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Shows a {@code Speaker: line} message like a conversation screen of the default conversation IO, e.g. MythicHUD,
 * in the conversation style's colors. Text without a speaker is narration and keeps its own colors; the
 * {@code speaker} option names its speaker, {@code speaker:unknown} uses {@code conversation.unknown_speaker}.
 * Announcement lines like "New Objective: ..." go to the {@code notice} notify IO.
 * The line is hidden {@code duration} seconds (default 2.5) after it is typed, unless another line replaced it.
 * Falls back to chat when the default conversation IO renders to chat.
 */
public class DialogueNotifyIO extends NotifyIO {

    /**
     * The longest speaker name, longer text before a colon is not a speaker.
     */
    private static final int MAX_SPEAKER = 40;

    /**
     * Serializer to split the message at line breaks while keeping the colors.
     */
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    /**
     * The plugin instance used for scheduling.
     */
    private final Plugin plugin;

    /**
     * The registry to find the default conversation IO in.
     */
    private final ConversationIORegistry conversationIORegistry;

    /**
     * The registry to find the notice notify IO in.
     */
    private final NotifyIORegistry notifyIORegistry;

    /**
     * The config with the default conversation IO and the unknown speaker name.
     */
    private final ConfigAccessor config;

    /**
     * Seconds to keep the line after it is typed.
     */
    private final Argument<Number> duration;

    /**
     * The speaker set with the {@code speaker} option, or null to take it from the message.
     */
    @Nullable
    private final String speakerOption;

    /**
     * Create a new dialogue notify IO.
     *
     * @param placeholders           the placeholder manager
     * @param pack                   the package of the notification
     * @param data                   the notify data
     * @param plugin                 the plugin instance used for scheduling
     * @param conversationIORegistry the registry to find the default conversation IO in
     * @param notifyIORegistry       the registry to find the notice notify IO in
     * @param config                 the config with the default conversation IO and the unknown speaker name
     * @throws QuestException if the data is invalid
     */
    public DialogueNotifyIO(final PlaceholderManager placeholders, @Nullable final QuestPackage pack, final Map<String, String> data,
                            final Plugin plugin, final ConversationIORegistry conversationIORegistry,
                            final NotifyIORegistry notifyIORegistry, final ConfigAccessor config) throws QuestException {
        super(placeholders, pack, data);
        this.plugin = plugin;
        this.conversationIORegistry = conversationIORegistry;
        this.notifyIORegistry = notifyIORegistry;
        this.config = config;
        this.duration = getNumberData("duration", 2.5);
        this.speakerOption = data.get("speaker");
    }

    /**
     * Splits the speaker off the first line: from the {@code speaker} option, or from {@code Speaker: text},
     * which then takes the conversation style's colors. Narration has no speaker and keeps its colors.
     *
     * @param first the first line
     * @return the speaker and the text
     */
    private SpokenLine spokenLine(final Component first) {
        if (speakerOption != null) {
            return new SpokenLine(Component.text("unknown".equals(speakerOption)
                    ? config.getString("conversation.unknown_speaker", "???") : speakerOption), first);
        }
        final String plain = PlainTextComponentSerializer.plainText().serialize(first);
        final int colon = plain.indexOf(':');
        if (colon > 0 && colon <= MAX_SPEAKER) {
            return new SpokenLine(Component.text(plain.substring(0, colon).trim()), Component.text(plain.substring(colon + 1).trim()));
        }
        return new SpokenLine(Component.empty(), first);
    }

    @Override
    protected void notifyPlayer(final Component message, final OnlineProfile onlineProfile) throws QuestException {
        final MenuConvIOFactory screen = MenuConvIOFactory.defaultScreen(conversationIORegistry, config);
        if (screen == null) {
            onlineProfile.getPlayer().sendMessage(message);
            return;
        }
        final NoticeLines split = NoticeLines.split(message);
        if (split.notice() != null) {
            notifyIORegistry.getFactory(List.of("notice")).create(pack, Map.of()).sendNotify(split.notice(), onlineProfile);
        }
        if (split.main() == null) {
            return;
        }
        final String legacy = LEGACY.serialize(split.main());
        final int lineBreak = legacy.indexOf('\n');
        final SpokenLine spoken = spokenLine(LEGACY.deserialize(lineBreak < 0 ? legacy : legacy.substring(0, lineBreak)));
        final Component speaker = spoken.speaker();
        final Component line = lineBreak < 0 ? spoken.text()
                : spoken.text().append(Component.newline()).append(LEGACY.deserialize(legacy.substring(lineBreak + 1)));
        final long holdTicks = Math.round(duration.getValue(onlineProfile).doubleValue() * 20);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                final long[] lineId = new long[1];
                lineId[0] = screen.showLine(onlineProfile, speaker, line, () -> plugin.getServer().getScheduler()
                        .runTaskLater(plugin, () -> screen.hideLine(onlineProfile, lineId[0]), holdTicks));
            } catch (final QuestException e) {
                plugin.getLogger().warning("Could not show dialogue line: " + e.getMessage());
            }
        });
    }

    /**
     * A line split into speaker and text.
     *
     * @param speaker the speaker, empty for narration
     * @param text    the text
     */
    private record SpokenLine(Component speaker, Component text) {
    }
}
