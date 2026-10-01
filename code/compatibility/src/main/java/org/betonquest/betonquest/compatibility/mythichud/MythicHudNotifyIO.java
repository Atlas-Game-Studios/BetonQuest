package org.betonquest.betonquest.compatibility.mythichud;

import io.lumine.mythichud.api.HudHolder;
import io.lumine.mythichud.api.MythicHUD;
import io.lumine.mythichud.api.element.popup.HudPopup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;
import org.betonquest.betonquest.notify.NotifyIO;
import org.betonquest.betonquest.notify.io.NoticeLines;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows a notification in a MythicHUD popup whose layers take the lines as {@code %arg-1%}, {@code %arg-2%}, ...
 * Lines are wrapped to fit, extra lines are dropped. The popup disappears after {@code duration} seconds.
 */
public class MythicHudNotifyIO extends NotifyIO {

    /**
     * Serializer to split the message at line breaks while keeping the colors.
     */
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    /**
     * The popup settings.
     */
    private final Settings settings;

    /**
     * Seconds to show the popup.
     */
    private final Argument<Number> duration;

    /**
     * Receives announcement lines split off the message, or null to show them in this popup.
     */
    @Nullable
    private final NoticeSender notices;

    /**
     * Create a new MythicHUD notify IO.
     *
     * @param placeholders the placeholder manager
     * @param pack         the package of the notification
     * @param data         the notify data
     * @param settings     the popup settings
     * @param notices      receives announcement lines split off the message, or null to show them in this popup
     * @throws QuestException if the data is invalid
     */
    public MythicHudNotifyIO(final PlaceholderManager placeholders, @Nullable final QuestPackage pack, final Map<String, String> data,
                             final Settings settings, @Nullable final NoticeSender notices) throws QuestException {
        super(placeholders, pack, data);
        this.settings = settings;
        this.duration = getNumberData("duration", settings.duration());
        this.notices = notices;
    }

    @Override
    protected void notifyPlayer(final Component message, final OnlineProfile onlineProfile) throws QuestException {
        Component shown = message;
        if (notices != null) {
            final NoticeLines split = NoticeLines.split(message);
            if (split.notice() != null) {
                notices.send(split.notice(), onlineProfile);
            }
            if (split.main() == null) {
                return;
            }
            shown = split.main();
        }
        final HudPopup popup = MythicHUD.getInstance().popups().get(settings.popup());
        if (popup == null) {
            throw new QuestException("MythicHUD popup '" + settings.popup() + "' does not exist");
        }
        final List<String> args = new ArrayList<>();
        for (final String line : LEGACY.serialize(shown).split("\n", -1)) {
            settings.wrapper().splitWidth(LEGACY.deserialize(line)).forEach(wrapped -> args.add(MiniMessage.miniMessage().serialize(wrapped)));
        }
        while (args.size() < settings.lines()) {
            args.add("");
        }
        final HudHolder holder = HudHolder.get(onlineProfile.getPlayer());
        holder.removePopup(settings.popup());
        holder.sendPopup(popup, (int) Math.round(duration.getValue(onlineProfile).doubleValue() * 20),
                args.subList(0, settings.lines()).toArray(String[]::new));
    }

    /**
     * The settings of a popup.
     *
     * @param popup    the MythicHUD popup key
     * @param lines    how many lines the popup has
     * @param wrapper  wraps lines to the popup width
     * @param duration the default seconds to show the popup
     */
    public record Settings(String popup, int lines, FixedComponentLineWrapper wrapper, int duration) {
    }

    /**
     * Sends announcement lines to another notify IO.
     */
    @FunctionalInterface
    public interface NoticeSender {

        /**
         * Sends the lines.
         *
         * @param notice        the announcement lines
         * @param onlineProfile the player
         * @throws QuestException if sending fails
         */
        void send(Component notice, OnlineProfile onlineProfile) throws QuestException;
    }
}
