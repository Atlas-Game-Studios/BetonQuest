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

import java.util.List;
import java.util.stream.Stream;

/**
 * Shows menu conversation lines in a MythicHUD popup.
 * The popup gets the NPC name as {@code %arg-1%} and the screen lines as {@code %arg-2%} onwards.
 */
public class MythicHudRenderer implements MenuConvIO.Renderer {

    /**
     * Popup lifetime in ticks. Every render replaces the popup, so a {@code refresh_delay} below this keeps it
     * visible; a bounded lifetime keeps MythicHUD's expiry tasks from piling up.
     */
    private static final int DURATION = 20 * 60 * 5;

    /**
     * The custom logger for this class.
     */
    private final BetonQuestLogger log;

    /**
     * The key of the MythicHUD popup.
     */
    private final String popupKey;

    /**
     * Creates a new MythicHUD renderer.
     *
     * @param log      the custom logger for this class
     * @param popupKey the key of the MythicHUD popup
     */
    public MythicHudRenderer(final BetonQuestLogger log, final String popupKey) {
        this.log = log;
        this.popupKey = popupKey;
    }

    @Override
    public void render(final Conversation conv, final OnlineProfile profile, final Component npcName, final List<Component> lines) {
        final HudPopup popup = MythicHUD.getInstance().popups().get(popupKey);
        if (popup == null) {
            log.warn("MythicHUD popup '" + popupKey + "' does not exist, cannot show conversation");
            return;
        }
        final String[] args = Stream.concat(Stream.of(npcName), lines.stream())
                .map(MiniMessage.miniMessage()::serialize)
                .toArray(String[]::new);
        final HudHolder holder = HudHolder.get(profile.getPlayer());
        holder.removePopup(popupKey);
        holder.sendPopup(popup, DURATION, args);
    }

    @Override
    public void hide(final OnlineProfile profile) {
        HudHolder.get(profile.getPlayer()).removePopup(popupKey);
    }
}
