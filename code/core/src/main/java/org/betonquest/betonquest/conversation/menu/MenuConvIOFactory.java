package org.betonquest.betonquest.conversation.menu;

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
import org.betonquest.betonquest.conversation.menu.input.ConversationAction;
import org.betonquest.betonquest.conversation.menu.input.ConversationSession;
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
     * Creates a menu factory with the same controls that shows its lines elsewhere.
     *
     * @param overrideSection config section whose keys override {@code conversation.io.menu}
     * @param renderer        where the display lines are shown
     * @return the new factory
     */
    public MenuConvIOFactory withRenderer(final String overrideSection, final MenuConvIO.Renderer renderer) {
        return new MenuConvIOFactory(loggerFactory, config, plugin, localizations, inputFunction, textParser, fontRegistry,
                colors, overrideSection, renderer);
    }

    @Override
    public ConversationIO parse(final Conversation conversation, final OnlineProfile onlineProfile) throws QuestException {
        final MenuConvIOSettings settings = MenuConvIOSettings.fromConfigurationSection(textParser, settingsSection());
        final FixedComponentLineWrapper componentLineWrapper = new FixedComponentLineWrapper(fontRegistry, settings.lineLength());
        return new MenuConvIO(loggerFactory.create(MenuConvIO.class), config, plugin, localizations, inputFunction, conversation, onlineProfile, colors, settings,
                componentLineWrapper, getControls(settings), renderer);
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
                    .map(s -> s.toUpperCase(Locale.ROOT))
                    .map(MenuConvIO.CONTROL::valueOf).toList();
        } catch (final IllegalArgumentException e) {
            throw new QuestException("Invalid data for '" + name + "': " + string, e);
        }
    }
}
