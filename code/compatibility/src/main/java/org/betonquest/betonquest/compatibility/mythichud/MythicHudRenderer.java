package org.betonquest.betonquest.compatibility.mythichud;

import io.lumine.mythichud.api.HudHolder;
import io.lumine.mythichud.api.MythicHUD;
import io.lumine.mythichud.api.element.popup.HudPopup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.conversation.Conversation;
import org.betonquest.betonquest.conversation.menu.MenuConvIO;
import org.betonquest.betonquest.conversation.menu.display.HudDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Shows conversations in MythicHUD popups: the dialogue box ({@code %arg-1%} the NPC name, then the lines),
 * the options box ({@code %arg-1%}... the option lines) and one hint popup per {@link HudDisplay.Hint},
 * named {@code <hint prefix><hint>} like {@code betonquest-hint-skip}.
 */
public class MythicHudRenderer implements MenuConvIO.Renderer {

    /**
     * How long the popups stay without updates, in ticks.
     */
    private static final int DURATION = 20 * 60 * 5;

    /**
     * Custom {@link BetonQuestLogger} instance for this class.
     */
    private final BetonQuestLogger log;

    /**
     * The dialogue box popup.
     */
    private final String popupKey;

    /**
     * The options box popup.
     */
    private final String optionsPopupKey;

    /**
     * The prefix of the hint popups.
     */
    private final String hintPrefix;

    /**
     * Create a new MythicHUD renderer.
     *
     * @param log             the logger
     * @param popupKey        the dialogue box popup
     * @param optionsPopupKey the options box popup
     * @param hintPrefix      the prefix of the hint popups
     */
    public MythicHudRenderer(final BetonQuestLogger log, final String popupKey, final String optionsPopupKey,
                             final String hintPrefix) {
        this.log = log;
        this.popupKey = popupKey;
        this.optionsPopupKey = optionsPopupKey;
        this.hintPrefix = hintPrefix;
    }

    @Override
    public void render(@Nullable final Conversation conv, final OnlineProfile profile, final Component npcName, final List<Component> lines) {
        send(profile, popupKey, Stream.concat(Stream.of(npcName), lines.stream()).toList());
    }

    @Override
    public boolean isHud() {
        return true;
    }

    @Override
    public void renderHud(@Nullable final Conversation conv, final OnlineProfile profile, final Component npcName,
                          final HudDisplay.HudScreen screen) {
        render(conv, profile, npcName, screen.lines());
        if (screen.options().isEmpty()) {
            HudHolder.get(profile.getPlayer()).removePopup(optionsPopupKey);
        } else {
            send(profile, optionsPopupKey, screen.options());
        }
        for (final HudDisplay.Hint hint : HudDisplay.Hint.values()) {
            if (hint != screen.hint() && hint != HudDisplay.Hint.NONE) {
                HudHolder.get(profile.getPlayer()).removePopup(hintPopup(hint));
            }
        }
        if (screen.hint() != HudDisplay.Hint.NONE) {
            send(profile, hintPopup(screen.hint()), List.of());
        }
    }

    @Override
    public void hide(final OnlineProfile profile) {
        final HudHolder holder = HudHolder.get(profile.getPlayer());
        holder.removePopup(popupKey);
        holder.removePopup(optionsPopupKey);
        for (final HudDisplay.Hint hint : HudDisplay.Hint.values()) {
            holder.removePopup(hintPopup(hint));
        }
    }

    private String hintPopup(final HudDisplay.Hint hint) {
        return hintPrefix + hint.name().toLowerCase(Locale.ROOT);
    }

    private void send(final OnlineProfile profile, final String key, final List<Component> args) {
        final HudPopup popup = MythicHUD.getInstance().popups().get(key);
        if (popup == null) {
            log.warn("MythicHUD popup '" + key + "' does not exist, cannot show conversation");
            return;
        }
        final HudHolder holder = HudHolder.get(profile.getPlayer());
        // Not removed first: MythicHUD shows the newest entry, and a remove leaves a gap other threads can send, which flashes
        holder.sendPopup(popup, DURATION, args.stream().map(MiniMessage.miniMessage()::serialize).toArray(String[]::new));
    }
}
