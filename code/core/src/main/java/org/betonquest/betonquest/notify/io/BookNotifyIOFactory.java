package org.betonquest.betonquest.notify.io;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.common.component.BookPageWrapper;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;
import org.betonquest.betonquest.kernel.registry.feature.NotifyIORegistry;
import org.betonquest.betonquest.notify.NotifyIO;
import org.betonquest.betonquest.notify.NotifyIOFactory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Factory to create {@link BookNotifyIO}s.
 */
public class BookNotifyIOFactory implements NotifyIOFactory {

    /**
     * The placeholder manager.
     */
    private final PlaceholderManager placeholders;

    /**
     * The plugin instance used for scheduling.
     */
    private final Plugin plugin;

    /**
     * The registry to find the notice notify IO in.
     */
    private final NotifyIORegistry notifyIORegistry;

    /**
     * Splits the text into book pages.
     */
    private final BookPageWrapper pageWrapper;

    /**
     * Create a new book notify IO factory.
     *
     * @param placeholders     the placeholder manager
     * @param plugin           the plugin instance used for scheduling
     * @param notifyIORegistry the registry to find the notice notify IO in
     * @param pageWrapper      splits the text into book pages
     */
    public BookNotifyIOFactory(final PlaceholderManager placeholders, final Plugin plugin,
                               final NotifyIORegistry notifyIORegistry, final BookPageWrapper pageWrapper) {
        this.placeholders = placeholders;
        this.plugin = plugin;
        this.notifyIORegistry = notifyIORegistry;
        this.pageWrapper = pageWrapper;
    }

    @Override
    public NotifyIO create(@Nullable final QuestPackage pack, final Map<String, String> categoryData) throws QuestException {
        return new BookNotifyIO(placeholders, pack, categoryData, plugin, notifyIORegistry, pageWrapper);
    }
}
