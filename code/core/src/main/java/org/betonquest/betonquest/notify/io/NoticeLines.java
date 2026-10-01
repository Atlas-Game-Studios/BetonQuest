package org.betonquest.betonquest.notify.io;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Splits announcement lines like "Quest Started: ..." or "Received: ..." off a message, so they can be shown
 * as a notice instead of with the rest. Announcements are the lines that start in gold or pink.
 *
 * @param main   the lines that are not announcements, or null if there are none
 * @param notice the announcement lines, or null if there are none
 */
public record NoticeLines(@Nullable Component main, @Nullable Component notice) {

    /**
     * Serializer to split the message at line breaks while keeping the colors.
     */
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    /**
     * Splits the announcement lines off a message.
     *
     * @param message the message
     * @return the split message
     */
    public static NoticeLines split(final Component message) {
        final Map<Boolean, List<String>> lines = Arrays.stream(LEGACY.serialize(message).split("\n", -1))
                .collect(Collectors.partitioningBy(NoticeLines::isAnnouncement));
        final List<String> main = lines.get(false);
        final List<String> notice = lines.get(true);
        return new NoticeLines(main.isEmpty() ? null : LEGACY.deserialize(String.join("\n", main)),
                notice.isEmpty() ? null : LEGACY.deserialize(String.join("\n", notice)));
    }

    private static boolean isAnnouncement(final String line) {
        final String stripped = line.replaceAll("^(§[k-orK-OR])*", "");
        return stripped.startsWith("§6") || stripped.startsWith("§d");
    }
}
