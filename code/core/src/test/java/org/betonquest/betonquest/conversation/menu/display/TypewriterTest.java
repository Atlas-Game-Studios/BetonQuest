package org.betonquest.betonquest.conversation.menu.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link Typewriter}.
 */
class TypewriterTest {

    private final Component line = Component.text("Hi ").append(Component.text("there", NamedTextColor.RED));

    @Test
    void cuts_inside_styled_child() {
        final Typewriter typewriter = new Typewriter(5);
        assertEquals(Component.text("Hi ").append(Component.text("th", NamedTextColor.RED)), typewriter.type(line), "should keep style");
        assertTrue(typewriter.isCut(), "should be cut");
    }

    @Test
    void lines_after_cut_are_empty() {
        final Typewriter typewriter = new Typewriter(5);
        typewriter.type(line);
        assertEquals(Component.empty(), typewriter.type(line), "following lines should be empty");
    }

    @Test
    void exact_budget_is_not_cut() {
        final Typewriter typewriter = new Typewriter(8);
        assertEquals(line, typewriter.type(line), "should be fully shown");
        assertFalse(typewriter.isCut(), "should not be cut");
    }
}
