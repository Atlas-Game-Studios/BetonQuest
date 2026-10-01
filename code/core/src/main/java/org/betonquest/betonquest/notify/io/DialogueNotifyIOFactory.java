package org.betonquest.betonquest.notify.io;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;
import org.betonquest.betonquest.kernel.registry.feature.NotifyIORegistry;
import org.betonquest.betonquest.notify.NotifyIO;
import org.betonquest.betonquest.notify.NotifyIOFactory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Factory to create {@link DialogueNotifyIO}s.
 */
public class DialogueNotifyIOFactory implements NotifyIOFactory {

    /**
     * The placeholder manager.
     */
    private final PlaceholderManager placeholders;

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
     * The config with the default conversation IO.
     */
    private final ConfigAccessor config;

    /**
     * Create a new dialogue notify IO factory.
     *
     * @param placeholders           the placeholder manager
     * @param plugin                 the plugin instance used for scheduling
     * @param conversationIORegistry the registry to find the default conversation IO in
     * @param notifyIORegistry       the registry to find the notice notify IO in
     * @param config                 the config with the default conversation IO
     */
    public DialogueNotifyIOFactory(final PlaceholderManager placeholders, final Plugin plugin,
                                   final ConversationIORegistry conversationIORegistry, final NotifyIORegistry notifyIORegistry,
                                   final ConfigAccessor config) {
        this.placeholders = placeholders;
        this.plugin = plugin;
        this.conversationIORegistry = conversationIORegistry;
        this.notifyIORegistry = notifyIORegistry;
        this.config = config;
    }

    @Override
    public NotifyIO create(@Nullable final QuestPackage pack, final Map<String, String> categoryData) throws QuestException {
        return new DialogueNotifyIO(placeholders, pack, categoryData, plugin, conversationIORegistry, notifyIORegistry, config);
    }
}
