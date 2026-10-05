package org.betonquest.betonquest.conversation.menu;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.common.component.font.FontRegistry;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.conversation.menu.display.HudDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shows single NPC lines without options through a {@link MenuConvIO.Renderer}, e.g. for cutscenes,
 * with the typewriter effect. Lines longer than the box are split into pages. Right-clicking reveals the rest
 * of the page while it is typing, otherwise it turns to the next page.
 */
public class LinePresenter implements Listener {

    /**
     * The plugin instance used for scheduling.
     */
    private final Plugin plugin;

    /**
     * The renderer to show the lines with.
     */
    private final MenuConvIO.Renderer renderer;

    /**
     * The font registry to wrap lines with.
     */
    private final FontRegistry fontRegistry;

    /**
     * The lines that are still typing, by player.
     */
    private final Map<UUID, TypingLine> typing = new ConcurrentHashMap<>();

    /**
     * The id of the shown line, by player.
     */
    private final Map<UUID, Long> shown = new ConcurrentHashMap<>();

    /**
     * The id of the next shown line.
     */
    private final AtomicLong nextId = new AtomicLong();

    /**
     * Create a new line presenter. It must be registered as a listener to skip typing on right-click.
     *
     * @param plugin       the plugin instance used for scheduling
     * @param renderer     the renderer to show the lines with
     * @param fontRegistry the font registry to wrap lines with
     */
    public LinePresenter(final Plugin plugin, final MenuConvIO.Renderer renderer, final FontRegistry fontRegistry) {
        this.plugin = plugin;
        this.renderer = renderer;
        this.fontRegistry = fontRegistry;
    }

    /**
     * Shows a line, replacing the previous one.
     *
     * @param settings      the menu settings for layout and typewriter effect
     * @param onlineProfile the player to show the line to
     * @param speaker       the name of the speaker, empty for narration
     * @param text          the line to show
     * @param onTyped       called once the line is fully shown, right away without typewriter effect
     * @return the id of the line, for {@link #hide(OnlineProfile, long)}
     */
    public long show(final MenuConvIOSettings settings, final OnlineProfile onlineProfile, final Component speaker,
                     final Component text, final Runnable onTyped) {
        final long lineId = nextId.incrementAndGet();
        shown.put(onlineProfile.getPlayerUUID(), lineId);
        final TypingLine previous = typing.remove(onlineProfile.getPlayerUUID());
        if (previous != null) {
            previous.cancel();
            previous.onTyped.run();
        }
        final FixedComponentLineWrapper wrapper = new FixedComponentLineWrapper(fontRegistry, settings.lineLength());
        final HudDisplay display = new HudDisplay(settings, wrapper, wrapper, 1, text, List.of());
        renderer.renderHud(null, onlineProfile, speaker, display.screen());
        if (display.isRead()) {
            onTyped.run();
            return lineId;
        }
        final TypingLine line = new TypingLine(settings, onlineProfile, speaker, display, onTyped);
        typing.put(onlineProfile.getPlayerUUID(), line);
        line.runTaskTimer(plugin, 1, 1);
        return lineId;
    }

    /**
     * Removes a line if no other line replaced it since.
     *
     * @param onlineProfile the player to hide the line from
     * @param lineId        the id {@link #show} returned
     */
    public void hide(final OnlineProfile onlineProfile, final long lineId) {
        if (shown.getOrDefault(onlineProfile.getPlayerUUID(), -1L) == lineId) {
            hide(onlineProfile);
        }
    }

    /**
     * Whether a line is shown and not hidden yet.
     *
     * @param onlineProfile the player
     * @return true if a line is shown
     */
    public boolean isShown(final OnlineProfile onlineProfile) {
        return shown.containsKey(onlineProfile.getPlayerUUID());
    }

    /**
     * Removes the shown line.
     *
     * @param onlineProfile the player to hide the line from
     */
    public void hide(final OnlineProfile onlineProfile) {
        shown.remove(onlineProfile.getPlayerUUID());
        final TypingLine line = typing.remove(onlineProfile.getPlayerUUID());
        if (line != null) {
            line.cancel();
        }
        renderer.hide(onlineProfile);
    }

    /**
     * Skips typing when right-clicking a block or the air.
     *
     * @param event the interact event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && event.getAction().isRightClick() && skip(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * Skips typing when right-clicking an entity.
     *
     * @param event the interact entity event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEntity(final PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && skip(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private boolean skip(final Player player) {
        final TypingLine line = typing.get(player.getUniqueId());
        if (line == null) {
            return false;
        }
        if (line.display.isTyping()) {
            line.display.revealAll();
        } else {
            line.display.nextPage();
        }
        line.update();
        return true;
    }

    /**
     * Types a line a few characters per tick.
     */
    private final class TypingLine extends BukkitRunnable {

        /**
         * The menu settings for the typewriter effect.
         */
        private final MenuConvIOSettings settings;

        /**
         * The player the line is shown to.
         */
        private final OnlineProfile onlineProfile;

        /**
         * The name of the speaker.
         */
        private final Component speaker;

        /**
         * The display of the line.
         */
        private final HudDisplay display;

        /**
         * Called once the line is fully shown.
         */
        private final Runnable onTyped;

        private TypingLine(final MenuConvIOSettings settings, final OnlineProfile onlineProfile, final Component speaker,
                           final HudDisplay display, final Runnable onTyped) {
            super();
            this.settings = settings;
            this.onlineProfile = onlineProfile;
            this.speaker = speaker;
            this.display = display;
            this.onTyped = onTyped;
        }

        @Override
        public void run() {
            if (!onlineProfile.getPlayer().isOnline()) {
                typing.remove(onlineProfile.getPlayerUUID(), this);
                cancel();
                return;
            }
            if (!display.isTyping()) {
                return;
            }
            display.reveal(settings.typewriterSpeed());
            if (settings.typewriterSound() != null) {
                onlineProfile.getPlayer().playSound(settings.typewriterSound(), Sound.Emitter.self());
            }
            update();
        }

        /**
         * Shows the current page; once the last page is shown completely, the line is typed.
         */
        private void update() {
            renderer.renderHud(null, onlineProfile, speaker, display.screen());
            if (display.isRead() && typing.remove(onlineProfile.getPlayerUUID(), this)) {
                cancel();
                onTyped.run();
            }
        }
    }
}
