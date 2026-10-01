package org.betonquest.betonquest.feature.questlog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the sidebar line wrapping of the {@link QuestLog}.
 */
class QuestLogTest {

    @Test
    void wrapsAtSpacesWithinWidth() {
        assertEquals(List.of("Deliver Akzir’s Supply Request", "to Captain Elfius in", "Karagport."),
                QuestLog.wrap("Deliver Akzir’s Supply Request to Captain Elfius in Karagport."), "should wrap at the last fitting space");
    }

    @Test
    void keepsShortTextOnOneLine() {
        assertEquals(List.of("Aid from Karagport:"), QuestLog.wrap("Aid from Karagport:"), "should not wrap short text");
    }

    @Test
    void putsLongWordsOnTheirOwnLine() {
        assertEquals(List.of("a", "abcdefghijklmnopqrstuvwxyzabcdefgh", "b"),
                QuestLog.wrap("a abcdefghijklmnopqrstuvwxyzabcdefgh b"), "should not split long words");
    }
}
