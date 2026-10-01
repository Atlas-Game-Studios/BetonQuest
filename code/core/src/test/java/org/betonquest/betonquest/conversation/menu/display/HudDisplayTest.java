package org.betonquest.betonquest.conversation.menu.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.betonquest.betonquest.api.common.component.ComponentFixture;
import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.common.component.VariableComponent;
import org.betonquest.betonquest.conversation.menu.MenuConvIOSettings;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the paging, typing, hints and option selection of {@link HudDisplay}.
 */
class HudDisplayTest {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private static MenuConvIOSettings settings(final int typewriterSpeed) {
        return new MenuConvIOSettings(100, 2, 0, 0, 1200, 10, typewriterSpeed, null, true, "none", "center", true, true,
                "right_click", "scroll", "", new VariableComponent(MINI.deserialize("{npc_name}")),
                new VariableComponent(MINI.deserialize("{npc_text}")), Component.empty(),
                new VariableComponent(MINI.deserialize("  {option_text}")), Component.empty(),
                new VariableComponent(MINI.deserialize("> {option_text}")), Component.empty(),
                Component.empty(), Component.empty(), 100, 2, 15);
    }

    private static HudDisplay display(final int typewriterSpeed, final String text, final List<Component> options) throws IOException {
        final FixedComponentLineWrapper wrapper = new FixedComponentLineWrapper(new ComponentFixture().getFontRegistry(), 100);
        return new HudDisplay(settings(typewriterSpeed), wrapper, wrapper, 2, Component.text(text), options);
    }

    private static List<String> plain(final List<Component> lines) {
        return lines.stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
    }

    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void pagesLongTextAndShowsOptionsOnLastPage() throws IOException {
        final HudDisplay display = display(0, "one two three four five six seven eight nine ten eleven twelve thirteen",
                List.of(Component.text("Yes"), Component.text("No")));
        final HudDisplay.HudScreen first = display.screen();
        assertEquals(2, first.lines().size(), "a page should fill the box");
        assertTrue(display.hasNextPage(), "the text should not fit one page");
        assertEquals(HudDisplay.Hint.CONTINUE, first.hint(), "right-click should turn the page");
        assertTrue(first.options().isEmpty(), "options should wait for the last page");
        while (display.hasNextPage()) {
            display.nextPage();
        }
        final HudDisplay.HudScreen last = display.screen();
        assertEquals(HudDisplay.Hint.SELECT, last.hint(), "right-click should select on the last page");
        assertEquals(List.of("> Yes", "  No"), plain(last.options()), "the first option should be selected");
    }

    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void typesBeforeContinuing() throws IOException {
        final HudDisplay display = display(1, "Hello", List.of());
        assertEquals(List.of("", ""), plain(display.screen().lines()), "nothing should be typed yet");
        assertEquals(HudDisplay.Hint.SKIP, display.hint(), "right-click should skip typing");
        display.reveal(2);
        assertEquals("He", plain(display.screen().lines()).get(0), "two characters should be typed");
        display.revealAll();
        assertEquals("Hello", plain(display.screen().lines()).get(0), "everything should be typed");
        assertTrue(display.isRead(), "a typed single page should be read");
        assertEquals(HudDisplay.Hint.NONE, display.hint(), "nothing should be left to do");
        display.setClosing(true);
        assertEquals(HudDisplay.Hint.CONTINUE, display.hint(), "right-click should close the last line");
    }

    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void movesSelectionAndScrollsOptionWindow() throws IOException {
        final HudDisplay display = display(0, "Hi", List.of(Component.text("A"), Component.text("B"), Component.text("C")));
        display.screen();
        display.moveSelection(false);
        assertEquals(2, display.getSelection(), "moving up from the first option should wrap to the last");
        assertEquals(List.of("  B", "> C"), plain(display.screen().options()), "the window should follow the selection");
        display.moveSelection(true);
        assertEquals(List.of("> A", "  B"), plain(display.screen().options()), "the window should scroll back up");
        assertFalse(display.hasNextPage(), "a short text should fit one page");
    }
}
