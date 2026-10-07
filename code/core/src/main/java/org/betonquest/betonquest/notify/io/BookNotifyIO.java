package org.betonquest.betonquest.notify.io;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.common.component.BookPageWrapper;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;
import org.betonquest.betonquest.kernel.registry.feature.NotifyIORegistry;
import org.betonquest.betonquest.notify.NotifyIO;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Opens the message as a written book, e.g. for lore books. The text is shown in the book's default dark color,
 * since chat colors are hard to read on paper. Announcement lines like "Discovered ..." go to the {@code notice}
 * notify IO.
 */
public class BookNotifyIO extends NotifyIO {

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
     * Create a new book notify IO.
     *
     * @param placeholders     the placeholder manager
     * @param pack             the package of the notification
     * @param data             the notify data
     * @param plugin           the plugin instance used for scheduling
     * @param notifyIORegistry the registry to find the notice notify IO in
     * @param pageWrapper      splits the text into book pages
     * @throws QuestException if the data is invalid
     */
    public BookNotifyIO(final PlaceholderManager placeholders, @Nullable final QuestPackage pack, final Map<String, String> data,
                        final Plugin plugin, final NotifyIORegistry notifyIORegistry, final BookPageWrapper pageWrapper)
            throws QuestException {
        super(placeholders, pack, data);
        this.plugin = plugin;
        this.notifyIORegistry = notifyIORegistry;
        this.pageWrapper = pageWrapper;
    }

    @Override
    protected void notifyPlayer(final Component message, final OnlineProfile onlineProfile) throws QuestException {
        final NoticeLines split = NoticeLines.split(message);
        if (split.notice() != null) {
            notifyIORegistry.getFactory(List.of("notice")).create(pack, Map.of()).sendNotify(split.notice(), onlineProfile);
        }
        if (split.main() == null) {
            return;
        }
        final Component text = Component.text(PlainTextComponentSerializer.plainText().serialize(split.main()));
        final Book book = Book.book(Component.empty(), Component.empty(), pageWrapper.splitPages(text));
        plugin.getServer().getScheduler().runTask(plugin, () -> onlineProfile.getPlayer().openBook(book));
    }
}
