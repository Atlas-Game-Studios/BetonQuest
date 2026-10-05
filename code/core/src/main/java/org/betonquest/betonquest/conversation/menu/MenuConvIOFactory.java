package org.betonquest.betonquest.conversation.menu;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.function.TriFunction;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.common.component.font.FontRegistry;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.config.Localizations;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.text.TextParser;
import org.betonquest.betonquest.conversation.Conversation;
import org.betonquest.betonquest.conversation.ConversationColors;
import org.betonquest.betonquest.conversation.ConversationIO;
import org.betonquest.betonquest.conversation.ConversationIOFactory;
import org.betonquest.betonquest.conversation.menu.display.HudDisplay;
import org.betonquest.betonquest.conversation.menu.input.ConversationAction;
import org.betonquest.betonquest.conversation.menu.input.ConversationSession;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Menu conversation output.
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.TooManyMethods"})
public class MenuConvIOFactory implements ConversationIOFactory {

    /**
     * The logger factory to create new logger instances.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * The config accessor to the plugin's configuration.
     */
    private final ConfigAccessor config;

    /**
     * Plugin instance to run tasks.
     */
    private final Plugin plugin;

    /**
     * The Localizations instance.
     */
    private final Localizations localizations;

    /**
     * Function to create the input object with actions.
     */
    private final TriFunction<Player, ConversationAction, Boolean, ConversationSession> inputFunction;

    /**
     * the text parser to parse the configuration text.
     */
    private final TextParser textParser;

    /**
     * The font registry to use in APIs that work with {@link net.kyori.adventure.text.Component}.
     */
    private final FontRegistry fontRegistry;

    /**
     * The colors used for the conversation.
     */
    private final ConversationColors colors;

    /**
     * Config section overriding keys of {@code conversation.io.menu}, or null to use it as is.
     */
    @Nullable
    private final String overrideSection;

    /**
     * Where the display lines are shown.
     */
    private final MenuConvIO.Renderer renderer;

    /**
     * Shows lines for {@link #showLine}, created on first use.
     */
    @Nullable
    private LinePresenter presenter;

    /**
     * Create a new Menu conversation IO factory.
     *
     * @param loggerFactory the logger factory to create new logger instances
     * @param config        the plugin configuration accessor
     * @param plugin        the plugin instance
     * @param localizations the Localizations instance
     * @param inputFunction the function to create the input object with actions
     * @param textParser    the text parser to parse the configuration text
     * @param fontRegistry  the font registry used for the conversation
     * @param colors        the colors used for the conversation
     */
    public MenuConvIOFactory(final BetonQuestLoggerFactory loggerFactory, final ConfigAccessor config, final Plugin plugin,
                             final Localizations localizations,
                             final TriFunction<Player, ConversationAction, Boolean, ConversationSession> inputFunction,
                             final TextParser textParser, final FontRegistry fontRegistry, final ConversationColors colors) {
        this(loggerFactory, config, plugin, localizations, inputFunction, textParser, fontRegistry, colors, null, MenuConvIO.Renderer.CHAT);
    }

    private MenuConvIOFactory(final BetonQuestLoggerFactory loggerFactory, final ConfigAccessor config, final Plugin plugin,
                              final Localizations localizations,
                              final TriFunction<Player, ConversationAction, Boolean, ConversationSession> inputFunction,
                              final TextParser textParser, final FontRegistry fontRegistry, final ConversationColors colors,
                              @Nullable final String overrideSection, final MenuConvIO.Renderer renderer) {
        this.loggerFactory = loggerFactory;
        this.config = config;
        this.plugin = plugin;
        this.localizations = localizations;
        this.inputFunction = inputFunction;
        this.textParser = textParser;
        this.fontRegistry = fontRegistry;
        this.colors = colors;
        this.overrideSection = overrideSection;
        this.renderer = renderer;
    }

    /**
     * Creates a menu factory that shows its lines elsewhere.
     *
     * @param overrideSection config section whose keys override {@code conversation.io.menu}
     * @param renderer        where the display lines are shown
     * @param inputFunction   the function to create the input object with actions
     * @return the new factory
     */
    public MenuConvIOFactory withRenderer(final String overrideSection, final MenuConvIO.Renderer renderer,
                                          final TriFunction<Player, ConversationAction, Boolean, ConversationSession> inputFunction) {
        return new MenuConvIOFactory(loggerFactory, config, plugin, localizations, inputFunction, textParser, fontRegistry,
                colors, overrideSection, renderer);
    }

    @Override
    public boolean usesChat() {
        return renderer == MenuConvIO.Renderer.CHAT;
    }

    @Override
    public ConversationIO parse(final Conversation conversation, final OnlineProfile onlineProfile) throws QuestException {
        final MenuConvIOSettings settings = MenuConvIOSettings.fromConfigurationSection(textParser, settingsSection());
        final FixedComponentLineWrapper componentLineWrapper = new FixedComponentLineWrapper(fontRegistry, settings.lineLength());
        return new MenuConvIO(loggerFactory.create(MenuConvIO.class), config, plugin, localizations, inputFunction, conversation, onlineProfile, colors, settings,
                componentLineWrapper, new FixedComponentLineWrapper(fontRegistry, settings.optionLineLength()), getControls(settings),
                conversationRenderer());
    }

    /**
     * The renderer for conversations. Ending one keeps a {@link #showLine} line that replaced its screen,
     * like an {@code io:dialogue} notify fired by the last option.
     *
     * @return the renderer
     */
    private MenuConvIO.Renderer conversationRenderer() {
        return new MenuConvIO.Renderer() {
            @Override
            public void render(@Nullable final Conversation conv, final OnlineProfile profile, final Component npcName,
                               final List<Component> lines) {
                renderer.render(conv, profile, npcName, lines);
            }

            @Override
            public boolean isHud() {
                return renderer.isHud();
            }

            @Override
            public void renderHud(@Nullable final Conversation conv, final OnlineProfile profile, final Component npcName,
                                  final HudDisplay.HudScreen screen) {
                renderer.renderHud(conv, profile, npcName, screen);
            }

            @Override
            public void hide(final OnlineProfile profile) {
                if (presenter == null || !presenter.isShown(profile)) {
                    renderer.hide(profile);
                }
            }
        };
    }

    /**
     * Shows a single NPC line without options the way this IO shows conversation screens,
     * including the typewriter effect, which right-clicking skips. A new line replaces the previous one.
     *
     * @param onlineProfile the player to show the line to
     * @param speaker       the name of the speaker, empty for narration
     * @param text          the line to show
     * @param onTyped       called once the line is fully shown
     * @return the id of the line, for {@link #hideLine(OnlineProfile, long)}
     * @throws QuestException if the settings are invalid
     */
    public long showLine(final OnlineProfile onlineProfile, final Component speaker, final Component text,
                         final Runnable onTyped) throws QuestException {
        final MenuConvIOSettings settings = MenuConvIOSettings.fromConfigurationSection(textParser, settingsSection());
        return linePresenter().show(settings, onlineProfile, speaker, text, onTyped);
    }

    /**
     * Removes a line shown with {@link #showLine} if no other line replaced it since.
     *
     * @param onlineProfile the player to hide the line from
     * @param lineId        the id {@link #showLine} returned
     */
    public void hideLine(final OnlineProfile onlineProfile, final long lineId) {
        linePresenter().hide(onlineProfile, lineId);
    }

    /**
     * Get the default conversation IO if it shows screens with its own renderer, like MythicHUD.
     *
     * @param registry the conversation IO registry
     * @param config   the config with {@code conversation.default_io}
     * @return the default IO, or null if it is not a menu IO or renders to chat
     * @throws QuestException if no default IO is registered
     */
    @Nullable
    public static MenuConvIOFactory defaultScreen(final ConversationIORegistry registry,
                                                  final ConfigAccessor config) throws QuestException {
        final ConversationIOFactory conversationIO = registry.getFactory(
                List.of(config.getString("conversation.default_io", "menu,tellraw").split(",")));
        return conversationIO instanceof final MenuConvIOFactory menu && !menu.usesChat() ? menu : null;
    }

    /**
     * Removes a line shown with {@link #showLine}.
     *
     * @param onlineProfile the player to hide the line from
     */
    public void hideLine(final OnlineProfile onlineProfile) {
        linePresenter().hide(onlineProfile);
    }

    private LinePresenter linePresenter() {
        // Only called on the main thread, by cutscene actions
        if (presenter == null) {
            presenter = new LinePresenter(plugin, renderer, fontRegistry);
            plugin.getServer().getPluginManager().registerEvents(presenter, plugin);
        }
        return presenter;
    }

    private ConfigurationSection settingsSection() {
        final ConfigurationSection menu = config.getConfigurationSection("conversation.io.menu");
        final ConfigurationSection override = overrideSection == null ? null : config.getConfigurationSection(overrideSection);
        if (override == null) {
            return menu;
        }
        final MemoryConfiguration merged = new MemoryConfiguration();
        menu.getValues(false).forEach(merged::set);
        override.getValues(false).forEach(merged::set);
        return merged;
    }

    private Map<MenuConvIO.CONTROL, MenuConvIO.ACTION> getControls(final MenuConvIOSettings settings) throws QuestException {
        final Map<MenuConvIO.CONTROL, MenuConvIO.ACTION> controls = new EnumMap<>(MenuConvIO.CONTROL.class);
        for (final MenuConvIO.CONTROL control : controls(settings.controlCancel(), "control_cancel")) {
            if (!controls.containsKey(control)) {
                controls.put(control, MenuConvIO.ACTION.CANCEL);
            }
        }
        for (final MenuConvIO.CONTROL control : controls(settings.controlSelect(), "control_select")) {
            if (!controls.containsKey(control)) {
                controls.put(control, MenuConvIO.ACTION.SELECT);
            }
        }
        for (final MenuConvIO.CONTROL control : controls(settings.controlMove(), "control_move")) {
            if (!controls.containsKey(control)) {
                controls.put(control, MenuConvIO.ACTION.MOVE);
            }
        }
        return controls;
    }

    private List<MenuConvIO.CONTROL> controls(final String string, final String name) throws QuestException {
        try {
            return Arrays.stream(string.split(","))
                    .filter(s -> !s.isBlank())
                    .map(s -> s.toUpperCase(Locale.ROOT))
                    .map(MenuConvIO.CONTROL::valueOf).toList();
        } catch (final IllegalArgumentException e) {
            throw new QuestException("Invalid data for '" + name + "': " + string, e);
        }
    }
}
