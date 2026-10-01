package org.betonquest.betonquest.conversation.menu;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.apache.commons.lang3.function.TriFunction;
import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.config.Localizations;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.conversation.ChatConvIO;
import org.betonquest.betonquest.conversation.Conversation;
import org.betonquest.betonquest.conversation.ConversationColors;
import org.betonquest.betonquest.conversation.ConversationState;
import org.betonquest.betonquest.conversation.menu.display.Display;
import org.betonquest.betonquest.conversation.menu.display.HudDisplay;
import org.betonquest.betonquest.conversation.menu.display.Scroll;
import org.betonquest.betonquest.conversation.menu.input.ConversationAction;
import org.betonquest.betonquest.conversation.menu.input.ConversationSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An {@link ChatConvIO} implementation that use player ingame movements to control the conversation.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CouplingBetweenObjects", "PMD.ExcessiveParameterList", "PMD.CyclomaticComplexity"})
public class MenuConvIO extends ChatConvIO {

    /**
     * How often to check whether the last line of a HUD conversation can close, in ticks.
     */
    private static final long CLOSE_CHECK_TICKS = 5;

    /**
     * The controls that are used in the conversation.
     */
    protected final Map<CONTROL, ACTION> controls;

    /**
     * Thread safety.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Plugin instance to schedule tasks.
     */
    private final Plugin plugin;

    /**
     * All players that are currently on cooldown are in this list.
     * The cooldown is used to prevent players from spamming through the conversation or skipping through it by accident.
     */
    private final List<Player> selectionCooldowns = new ArrayList<>();

    /**
     * The settings for this conversation IO.
     */
    private final MenuConvIOSettings settings;

    /**
     * The component line wrapper to use for the conversation.
     */
    private final FixedComponentLineWrapper componentLineWrapper;

    /**
     * The input object triggering actions.
     */
    private final ConversationSession input;

    /**
     * Where the display lines are shown.
     */
    private final Renderer renderer;

    /**
     * The current state of the conversation.
     */
    @SuppressWarnings("PMD.AvoidUsingVolatile")
    protected volatile ConversationState state = ConversationState.CREATED;

    /**
     * The runnable that updates the display.
     */
    @Nullable
    protected BukkitRunnable displayRunnable;

    /**
     * The runnable that types the NPC text.
     */
    @Nullable
    protected BukkitRunnable typingRunnable;

    /**
     * The display used to show the conversation.
     */
    @Nullable
    protected Display chatDisplay;

    /**
     * The paged display used instead of {@link #chatDisplay} for HUD renderers.
     */
    @Nullable
    protected HudDisplay hudDisplay;

    /**
     * Wraps the options to the width of the options box of HUD renderers.
     */
    private final FixedComponentLineWrapper optionWrapper;

    /**
     * The end callback while a HUD conversation's last line waits to be closed, null otherwise.
     */
    @Nullable
    private Runnable pendingEnd;

    /**
     * Closes the last line of a HUD conversation when the player does nothing.
     */
    @Nullable
    private BukkitTask closeTask;

    /**
     * Creates a new MenuConvIO instance.
     *
     * @param log                  the logger that will be used for logging
     * @param config               the plugin configuration accessor
     * @param plugin               the plugin instance
     * @param localizations        the Localizations instance
     * @param inputFunction        the function to create the input object with actions
     * @param conv                 the conversation this IO is part of
     * @param onlineProfile        the online profile of the player participating in the conversation
     * @param colors               the colors used in the conversation
     * @param settings             the settings for the conversation IO
     * @param componentLineWrapper the component line wrapper to use for the conversation
     * @param optionWrapper        wraps the options to the width of the options box of HUD renderers
     * @param controls             the used controls
     * @param renderer             where the display lines are shown
     */
    public MenuConvIO(final BetonQuestLogger log, final ConfigAccessor config, final Plugin plugin,
                      final Localizations localizations,
                      final TriFunction<Player, ConversationAction, Boolean, ConversationSession> inputFunction,
                      final Conversation conv, final OnlineProfile onlineProfile, final ConversationColors colors,
                      final MenuConvIOSettings settings, final FixedComponentLineWrapper componentLineWrapper,
                      final FixedComponentLineWrapper optionWrapper, final Map<CONTROL, ACTION> controls, final Renderer renderer) {
        super(log, config, plugin, localizations, conv, onlineProfile, colors);
        this.plugin = plugin;
        this.settings = settings;
        this.componentLineWrapper = componentLineWrapper;
        this.optionWrapper = optionWrapper;
        this.controls = controls;
        this.renderer = renderer;
        this.input = inputFunction.apply(onlineProfile.getPlayer(), new MenuConversationAction(), settings.setSpeed());
    }

    private void start() {
        if (state.isStarted()) {
            return;
        }

        lock.lock();
        try {
            if (state.isStarted()) {
                return;
            }
            state = ConversationState.ACTIVE;
            input.begin();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void display() {
        if (Component.empty().equals(npcText) && options.isEmpty()) {
            end(() -> {
            });
            return;
        }

        if (!options.isEmpty() || renderer.isHud()) {
            start();
        }

        updateDisplay();
        if (settings.refreshDelay() > 0) {
            displayRunnable = new BukkitRunnable() {

                @Override
                public void run() {
                    updateDisplay();

                    if (state.isEnded()) {
                        this.cancel();
                    }
                }
            };
            displayRunnable.runTaskTimerAsynchronously(plugin, settings.refreshDelay(), settings.refreshDelay());
        }
        if (settings.typewriterSpeed() > 0) {
            startTyping();
        }
    }

    private void startTyping() {
        if (typingRunnable != null) {
            typingRunnable.cancel();
        }
        typingRunnable = new BukkitRunnable() {

            @Override
            public void run() {
                if (state.isEnded() || !isTyping()) {
                    this.cancel();
                    return;
                }
                if (hudDisplay != null) {
                    hudDisplay.reveal(settings.typewriterSpeed());
                } else if (chatDisplay != null) {
                    chatDisplay.reveal(settings.typewriterSpeed());
                }
                updateDisplay();
                if (settings.typewriterSound() != null) {
                    onlineProfile.getPlayer().playSound(settings.typewriterSound(), Sound.Emitter.self());
                }
            }
        };
        typingRunnable.runTaskTimerAsynchronously(plugin, 1, 1);
    }

    private boolean isTyping() {
        return hudDisplay != null ? hudDisplay.isTyping() : chatDisplay != null && chatDisplay.isTyping();
    }

    private void cancelRunnables() {
        if (displayRunnable != null) {
            displayRunnable.cancel();
            displayRunnable = null;
        }
        if (typingRunnable != null) {
            typingRunnable.cancel();
            typingRunnable = null;
        }
    }

    // Override this event from our parent
    @SuppressWarnings("deprecation")
    @Override
    @EventHandler(ignoreCancelled = true)
    public void onReply(final AsyncPlayerChatEvent event) {
        // Empty
    }

    @Override
    public void clear() {
        cancelRunnables();

        chatDisplay = null;
        hudDisplay = null;

        super.clear();
    }

    @Override
    public void end(final Runnable callback) {
        if (state.isEnded() || pendingEnd != null) {
            return;
        }
        if (hudDisplay != null && options.isEmpty()) {
            // Keep the last line until the player has read it and right-clicks, or does nothing for a while
            pendingEnd = callback;
            hudDisplay.setClosing(true);
            scheduleClose();
            updateDisplay();
            return;
        }
        finishEnd(callback);
    }

    /**
     * (Re)starts closing the last line {@code close_timeout} seconds after it is read completely.
     */
    private void scheduleClose() {
        if (closeTask != null) {
            closeTask.cancel();
        }
        final long timeoutTicks = Math.round(settings.closeTimeout() * 20);
        closeTask = new BukkitRunnable() {
            /**
             * Ticks since the last line was read completely.
             */
            private long readTicks;

            @Override
            public void run() {
                final HudDisplay display = hudDisplay;
                readTicks = display != null && !display.isRead() ? 0 : readTicks + CLOSE_CHECK_TICKS;
                final Runnable callback = pendingEnd;
                if (callback != null && readTicks >= timeoutTicks) {
                    finishEnd(callback);
                }
            }
        }.runTaskTimer(plugin, CLOSE_CHECK_TICKS, CLOSE_CHECK_TICKS);
    }

    private void finishEnd(final Runnable callback) {
        if (state.isEnded()) {
            return;
        }
        pendingEnd = null;
        if (closeTask != null) {
            closeTask.cancel();
            closeTask = null;
        }
        lock.lock();
        try {
            if (state.isEnded()) {
                return;
            }
            state = ConversationState.ENDED;
            input.end();

            cancelRunnables();

            renderer.hide(onlineProfile);
            super.end(callback);
        } finally {
            lock.unlock();
        }
    }

    private void passPlayerAnswer() {
        if (hudDisplay != null) {
            passHudAnswer(hudDisplay);
            return;
        }
        if (chatDisplay == null || isOnCooldown()) {
            return;
        }
        if (chatDisplay.isTyping()) {
            chatDisplay.revealAll();
            updateDisplay();
            return;
        }
        chatDisplay.getSelection().ifPresent(index -> conv.passPlayerAnswer(index + 1));
    }

    private void passHudAnswer(final HudDisplay display) {
        if (isOnCooldown()) {
            return;
        }
        if (pendingEnd != null) {
            scheduleClose();
        }
        if (display.isTyping()) {
            display.revealAll();
            updateDisplay();
        } else if (display.hasNextPage()) {
            display.nextPage();
            updateDisplay();
            if (settings.typewriterSpeed() > 0) {
                startTyping();
            }
        } else if (pendingEnd != null) {
            finishEnd(pendingEnd);
        } else if (display.getSelection() >= 0) {
            conv.passPlayerAnswer(display.getSelection() + 1);
        }
    }

    /**
     * Handles the player interact event.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerInteractEvent(final PlayerInteractEvent event) {
        if (state.isInactive() || !event.getPlayer().equals(onlineProfile.getPlayer())) {
            return;
        }

        lock.lock();
        try {
            if (state.isInactive()) {
                return;
            }

            event.setCancelled(true);

            final Action action = event.getAction();
            final CONTROL control = action.isLeftClick() ? CONTROL.LEFT_CLICK : CONTROL.RIGHT_CLICK;
            if (action != Action.PHYSICAL && event.getHand() == EquipmentSlot.HAND && controls.containsKey(control)) {
                handleSteering(controls.get(control));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Handles the player interact entity event.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerInteractEntityEvent(final PlayerInteractEntityEvent event) {
        if (state.isInactive() || !event.getPlayer().equals(onlineProfile.getPlayer())) {
            return;
        }

        lock.lock();
        try {
            if (state.isInactive()) {
                return;
            }

            event.setCancelled(true);

            final CONTROL control = controls.containsKey(CONTROL.RIGHT_CLICK) ? CONTROL.RIGHT_CLICK : CONTROL.LEFT_CLICK;
            if (event.getHand() == EquipmentSlot.HAND && controls.containsKey(control)) {
                handleSteering(controls.get(control));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Handles the entity damage by entity event.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void entityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
        if (state.isInactive() || !event.getDamager().equals(onlineProfile.getPlayer())) {
            return;
        }

        lock.lock();
        try {
            if (state.isInactive()) {
                return;
            }

            event.setCancelled(true);

            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && controls.containsKey(CONTROL.LEFT_CLICK)) {
                handleSteering(controls.get(CONTROL.LEFT_CLICK));
            }
        } finally {
            lock.unlock();
        }
    }

    private void handleSteering(final ACTION action) {
        switch (action) {
            case CANCEL -> {
                if (!conv.isMovementBlock()) {
                    conv.endConversation();
                }
            }
            case SELECT -> {
                passPlayerAnswer();
            }
            default -> {
            }
        }
    }

    private boolean isOnCooldown() {
        final Player player = onlineProfile.getPlayer();
        if (selectionCooldowns.contains(player)) {
            return true;
        }
        selectionCooldowns.add(player);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> selectionCooldowns.remove(player), settings.rateLimit());
        return false;
    }

    /**
     * Handles the player item held event.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerItemHeldEvent(final PlayerItemHeldEvent event) {
        if (state.isInactive() || !event.getPlayer().equals(onlineProfile.getPlayer())) {
            return;
        }
        if (!controls.containsKey(CONTROL.SCROLL)) {
            return;
        }

        lock.lock();
        try {
            if (state.isInactive()) {
                return;
            }

            event.setCancelled(true);

            updateDisplay(getScrollDirection(event.getPreviousSlot(), event.getNewSlot()));
        } finally {
            lock.unlock();
        }
    }

    private void updateDisplay() {
        updateDisplay(Scroll.NONE);
    }

    private void updateDisplay(final Scroll scroll) {
        if (renderer.isHud()) {
            updateHudDisplay(scroll);
            return;
        }
        if (state.isEnded() || scroll != Scroll.NONE && (chatDisplay == null || chatDisplay.isTyping())) {
            return;
        }
        if (chatDisplay == null) {
            chatDisplay = new Display(settings, componentLineWrapper, npcName, npcText, new ArrayList<>(options.values()));
        }
        renderer.render(conv, onlineProfile, npcName, chatDisplay.getDisplay(scroll));
    }

    private void updateHudDisplay(final Scroll scroll) {
        if (state.isEnded()) {
            return;
        }
        if (hudDisplay == null) {
            hudDisplay = new HudDisplay(settings, componentLineWrapper, optionWrapper, settings.optionLineCount(),
                    npcText, new ArrayList<>(options.values()));
        }
        if (scroll != Scroll.NONE) {
            hudDisplay.moveSelection(scroll == Scroll.DOWN);
        }
        renderer.renderHud(conv, onlineProfile, npcName, hudDisplay.screen());
    }

    private Scroll getScrollDirection(final int start, final int end) {
        for (int offset = 1; offset <= 4; offset++) {
            if ((start + offset) % 9 == end) {
                return Scroll.DOWN;
            }
        }
        return Scroll.UP;
    }

    /**
     * Shows the display lines to the player.
     */
    @FunctionalInterface
    public interface Renderer {

        /**
         * Sends the lines as one chat message.
         */
        Renderer CHAT = (conv, profile, npcName, lines) -> conv.sendMessage(Component.join(JoinConfiguration.newlines(), lines));

        /**
         * Shows the current screen. May be called off the main thread.
         *
         * @param conv    the conversation, or null for a cutscene line (never passed to {@link #CHAT})
         * @param profile the player to show it to
         * @param npcName the name of the NPC
         * @param lines   the lines of the current screen
         */
        void render(@Nullable Conversation conv, OnlineProfile profile, Component npcName, List<Component> lines);

        /**
         * Removes whatever {@link #render} showed, called once when the conversation ends.
         *
         * @param profile the player to hide it from
         */
        default void hide(final OnlineProfile profile) {
            // Chat needs no cleanup
        }

        /**
         * Whether this renderer shows a paged dialogue box with a separate options box, see {@link #renderHud}.
         *
         * @return true to get {@link #renderHud} calls instead of {@link #render} in conversations
         */
        default boolean isHud() {
            return false;
        }

        /**
         * Shows a paged HUD screen. May be called off the main thread.
         *
         * @param conv    the conversation, or null for a cutscene line
         * @param profile the player to show it to
         * @param npcName the name of the NPC
         * @param screen  the screen to show
         */
        default void renderHud(@Nullable final Conversation conv, final OnlineProfile profile, final Component npcName,
                               final HudDisplay.HudScreen screen) {
            render(conv, profile, npcName, screen.lines());
        }
    }

    /**
     * The actions that can be performed in the menu conversation.
     */
    public enum ACTION {
        /**
         * The player selected an option.
         */
        SELECT,
        /**
         * The player canceled the conversation.
         */
        CANCEL,
        /**
         * The player moved in the conversation.
         */
        MOVE
    }

    /**
     * The controls that can be used in the menu conversation.
     */
    public enum CONTROL {
        /**
         * The player jumped.
         */
        JUMP,
        /**
         * The player sneaked.
         */
        SNEAK,
        /**
         * The player scrolled.
         */
        SCROLL,
        /**
         * The player moved.
         */
        MOVE,
        /**
         * The player left-clicked.
         */
        LEFT_CLICK,
        /**
         * The player right-clicked.
         */
        RIGHT_CLICK
    }

    /**
     * Menu specific controls.
     */
    private final class MenuConversationAction implements ConversationAction {

        /**
         * The empty default constructor.
         */
        private MenuConversationAction() {
        }

        @Override
        public void unmount() {
            if (controls.containsKey(CONTROL.SNEAK)) {
                switch (controls.get(CONTROL.SNEAK)) {
                    case CANCEL:
                        if (!conv.isMovementBlock()) {
                            conv.endConversation();
                        }
                        break;
                    case SELECT:
                        lock.lock();
                        try {
                            passPlayerAnswer();
                        } finally {
                            lock.unlock();
                        }
                        break;
                    case MOVE:
                        break;
                }
            }
        }

        @Override
        public void jump() {
            if (controls.containsKey(CONTROL.JUMP)) {
                switch (controls.get(CONTROL.JUMP)) {
                    case CANCEL:
                        if (!conv.isMovementBlock()) {
                            conv.endConversation();
                        }
                        break;
                    case SELECT:
                        lock.lock();
                        try {
                            passPlayerAnswer();
                        } finally {
                            lock.unlock();
                        }
                        break;
                    case MOVE:
                        break;
                }
            }
        }

        @Override
        public void forward() {
            if (controls.containsKey(CONTROL.MOVE)) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> updateDisplay(Scroll.UP));
            }
        }

        @Override
        public void back() {
            if (controls.containsKey(CONTROL.MOVE)) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> updateDisplay(Scroll.DOWN));
            }
        }

        @Override
        public void left() {
            // Empty
        }

        @Override
        public void right() {
            // Empty
        }
    }
}
