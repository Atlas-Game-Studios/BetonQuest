package org.betonquest.betonquest.compatibility.mythichud;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.conversation.ConversationIOFactory;
import org.betonquest.betonquest.conversation.menu.MenuConvIOFactory;
import org.betonquest.betonquest.conversation.menu.input.ConversationSession;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;

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
                new MythicHudRenderer(api.loggerFactory().create(MythicHudRenderer.class), popup),
                (player, action, setSpeed) -> FREE_MOVEMENT));
    }

    @Override
    public void disable() {
        // Empty
    }
}
