package org.betonquest.betonquest.notify.io;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link NoticeLines}.
 */
class NoticeLinesTest {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private static String asLegacy(final Component component) {
        return LEGACY.serialize(component);
    }

    @Test
    void splitsAnnouncementsOffDialogue() {
        final NoticeLines split = NoticeLines.split(LEGACY.deserialize(
                "&aTyrriel&b: Where am I?\n&6Quest Started: &eThrough the Void\n&6Objective: &eFollow the path."));
        assertEquals("&aTyrriel&b: Where am I?", asLegacy(split.main()), "dialogue should stay");
        assertEquals("&6Quest Started: &eThrough the Void\n&6Objective: &eFollow the path.", asLegacy(split.notice()),
                "announcements should be split off");
    }

    @Test
    void keepsOtherLinesInMain() {
        final NoticeLines split = NoticeLines.split(LEGACY.deserialize("&7You force the door open\n&aRefugee&b: It burns!"));
        assertEquals("&7You force the door open\n&aRefugee&b: It burns!", asLegacy(split.main()), "dialogue lines should stay");
        assertNull(split.notice(), "there should be no notice");
    }

    @Test
    void splitsAnnouncementsAroundDialogue() {
        final NoticeLines split = NoticeLines.split(LEGACY.deserialize(
                "&6Quest Completed: &eThrough the Void\n&aRefugee&b: Hey, you're alive!\n&6Quest Started: &eA New Beginning"));
        assertEquals("&aRefugee&b: Hey, you're alive!", asLegacy(split.main()), "the dialogue should stay");
        assertEquals("&6Quest Completed: &eThrough the Void\n&6Quest Started: &eA New Beginning", asLegacy(split.notice()),
                "announcements before and after it should be split off");
    }

    @Test
    void hasNoMainWithOnlyAnnouncements() {
        final NoticeLines split = NoticeLines.split(LEGACY.deserialize("&6Quest Completed: &eAid"));
        assertNull(split.main(), "there should be nothing left");
        assertEquals("&6Quest Completed: &eAid", asLegacy(split.notice()), "the announcement should be split off");
    }
}
