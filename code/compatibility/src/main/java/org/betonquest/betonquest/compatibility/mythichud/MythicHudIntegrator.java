package org.betonquest.betonquest.compatibility.mythichud;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;
import org.betonquest.betonquest.conversation.ConversationIOFactory;
import org.betonquest.betonquest.conversation.menu.MenuConvIOFactory;
import org.betonquest.betonquest.conversation.menu.input.ConversationSession;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;
import org.betonquest.betonquest.kernel.registry.feature.NotifyIORegistry;

import java.util.List;
import java.util.Map;

/**
 * Integrator for MythicHUD.
 */
public class MythicHudIntegrator implements Integration {

    /**
     * The config section of the conversation IO, overriding keys of {@code conversation.io.menu}.
     */
    private static final String SECTION = "conversation.io.mythichud";

    /**
     * Does not mount the player, so they can walk away to end the conversation like with tellraw.
     */
    private static final ConversationSession FREE_MOVEMENT = new ConversationSession() {
        @Override
        public void begin() {
            // Empty
        }

        @Override
        public void end() {
            // Empty
        }
    };

    /**
     * The empty default constructor.
     */
    public MythicHudIntegrator() {
    }

    @Override
    public void enable(final BetonQuestApi api) {
        // Empty
    }

    @Override
    public void postEnable(final BetonQuestApi api) throws QuestException {
        final BetonQuest plugin = BetonQuest.getInstance();
        final ConversationIORegistry registry = plugin.getComponentLoader().get(ConversationIORegistry.class);
        final ConversationIOFactory menu = registry.getFactory("menu");
        if (!(menu instanceof final MenuConvIOFactory menuFactory)) {
            throw new QuestException("The 'menu' conversation IO is not available, it requires Minecraft 1.21.4+");
        }
        final String popup = plugin.getPluginConfig().getString(SECTION + ".popup", "betonquest-conversation");
        registry.register("mythichud", menuFactory.withRenderer(SECTION,
                new MythicHudRenderer(api.loggerFactory().create(MythicHudRenderer.class), popup,
                        plugin.getPluginConfig().getString(SECTION + ".options_popup", "betonquest-options"),
                        plugin.getPluginConfig().getString(SECTION + ".hint_popup_prefix", "betonquest-hint-")),
                (player, action, setSpeed) -> FREE_MOVEMENT));

        final NotifyIORegistry notifyIORegistry = plugin.getComponentLoader().get(NotifyIORegistry.class);
        final PlaceholderManager placeholders = api.placeholders().manager();
        final MythicHudNotifyIO.Settings notice = popupSettings(plugin.getPluginConfig(), api, "notice", 6, 200, 8);
        final MythicHudNotifyIO.Settings thought = popupSettings(plugin.getPluginConfig(), api, "thought", 4, 280, 6);
        notifyIORegistry.register("notice", (pack, data) -> new MythicHudNotifyIO(placeholders, pack, data, notice, null));
        notifyIORegistry.register("thought", (pack, data) -> new MythicHudNotifyIO(placeholders, pack, data, thought,
                (lines, profile) -> notifyIORegistry.getFactory(List.of("notice")).create(pack, Map.of()).sendNotify(lines, profile)));
    }

    private MythicHudNotifyIO.Settings popupSettings(final ConfigAccessor config, final BetonQuestApi api, final String name,
                                                     final int lines, final int lineLength, final int duration) {
        final String prefix = SECTION + "." + name + "_";
        return new MythicHudNotifyIO.Settings(config.getString(prefix + "popup", "betonquest-" + name),
                config.getInt(prefix + "lines", lines),
                new FixedComponentLineWrapper(api.fonts(), config.getInt(prefix + "line_length", lineLength)),
                config.getInt(prefix + "duration", duration));
    }

    @Override
    public void disable() {
        // Empty
    }
}
