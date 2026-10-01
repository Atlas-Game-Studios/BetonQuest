package org.betonquest.betonquest.conversation.menu.display;

import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.common.component.VariableReplacement;
import org.betonquest.betonquest.conversation.menu.MenuConvIOSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * The paged layout of a HUD conversation screen: the NPC text is split into pages that fill the dialogue box,
 * and the options are shown in their own box once the last page is typed. Right-clicking skips typing,
 * then turns the page, then selects the option.
 */
@SuppressWarnings("PMD.AvoidUsingVolatile")
public class HudDisplay {

    /**
     * The NPC text pages, each with {@link MenuConvIOSettings#lineCount()} lines at most.
     */
    private final List<List<Component>> pages;

    /**
     * The lines of the unselected options, by option.
     */
    private final List<List<Component>> options;

    /**
     * The lines of the selected options, by option.
     */
    private final List<List<Component>> selectedOptions;

    /**
     * How many lines the dialogue box has.
     */
    private final int lineCount;

    /**
     * How many lines the options box has.
     */
    private final int optionLineCount;

    /**
     * Characters typed per tick, 0 shows the text at once.
     */
    private final int typewriterSpeed;

    /**
     * The current page.
     */
    private final AtomicInteger page = new AtomicInteger();

    /**
     * Characters of the current page revealed by the typewriter effect.
     */
    private volatile int revealed;

    /**
     * If the current page was cut off by the typewriter effect when it was last shown.
     */
    private volatile boolean typing;

    /**
     * The selected option.
     */
    private final AtomicInteger selected = new AtomicInteger();

    /**
     * The first shown line of the options.
     */
    private volatile int optionTop;

    /**
     * Whether right-clicking the last page closes the screen.
     */
    private volatile boolean closing;

    /**
     * Create a new HUD display.
     *
     * @param settings        the menu settings for formatting, line count and typewriter speed
     * @param wrapper         wraps the NPC text to the dialogue box width
     * @param optionWrapper   wraps the options to the options box width
     * @param optionLineCount how many lines the options box has
     * @param npcText         the NPC text
     * @param options         the options, may be empty
     */
    public HudDisplay(final MenuConvIOSettings settings, final FixedComponentLineWrapper wrapper,
                      final FixedComponentLineWrapper optionWrapper, final int optionLineCount,
                      final Component npcText, final List<Component> options) {
        this.lineCount = Math.max(1, settings.lineCount());
        this.optionLineCount = Math.max(1, optionLineCount);
        this.typewriterSpeed = settings.typewriterSpeed();
        final List<Component> lines = wrapper.splitWidth(settings.npcText().resolve(new VariableReplacement("npc_text", npcText)),
                prefix(settings.npcTextWrap()));
        this.pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += lineCount) {
            pages.add(lines.subList(i, Math.min(lines.size(), i + lineCount)));
        }
        if (pages.isEmpty()) {
            pages.add(List.of());
        }
        this.options = new ArrayList<>();
        this.selectedOptions = new ArrayList<>();
        for (final Component option : options) {
            final VariableReplacement replacement = new VariableReplacement("option_text", option);
            this.options.add(optionWrapper.splitWidth(settings.optionText().resolve(replacement), prefix(settings.optionTextWrap())));
            selectedOptions.add(optionWrapper.splitWidth(settings.optionSelectedText().resolve(replacement),
                    prefix(settings.optionSelectedTextWrap())));
        }
        this.revealed = typewriterSpeed > 0 ? 0 : Integer.MAX_VALUE;
        this.typing = typewriterSpeed > 0;
    }

    private static Supplier<Component> prefix(final Component component) {
        final AtomicBoolean first = new AtomicBoolean(true);
        return () -> first.getAndSet(false) ? Component.empty() : component;
    }

    /**
     * Reveals more characters of the typewriter effect.
     *
     * @param characters how many characters to reveal
     */
    public void reveal(final int characters) {
        revealed = (int) Math.min(Integer.MAX_VALUE, (long) revealed + characters);
    }

    /**
     * Reveals the whole current page.
     */
    public void revealAll() {
        revealed = Integer.MAX_VALUE;
        typing = false;
    }

    /**
     * Whether the current page is still being typed, as of the last {@link #screen()}.
     *
     * @return true if typing
     */
    public boolean isTyping() {
        return typing;
    }

    /**
     * Whether there are pages after the current one.
     *
     * @return true if there is a next page
     */
    public boolean hasNextPage() {
        return page.get() < pages.size() - 1;
    }

    /**
     * Turns to the next page and starts typing it.
     */
    public void nextPage() {
        if (hasNextPage()) {
            page.incrementAndGet();
            revealed = typewriterSpeed > 0 ? 0 : Integer.MAX_VALUE;
            typing = typewriterSpeed > 0;
        }
    }

    /**
     * Whether the options are shown: on the last page, once it is typed.
     *
     * @return true if the options are shown
     */
    public boolean showsOptions() {
        return !options.isEmpty() && !hasNextPage() && !typing;
    }

    /**
     * Moves the selection, wrapping around.
     *
     * @param down true to move down, false to move up
     */
    public void moveSelection(final boolean down) {
        if (showsOptions()) {
            selected.updateAndGet(current -> Math.floorMod(current + (down ? 1 : -1), options.size()));
        }
    }

    /**
     * Get the selected option.
     *
     * @return the index of the selected option, or -1 if the options are not shown
     */
    public int getSelection() {
        return showsOptions() ? selected.get() : -1;
    }

    /**
     * Sets whether right-clicking the last page closes the screen.
     *
     * @param closing true if right-clicking the last page closes the screen
     */
    public void setClosing(final boolean closing) {
        this.closing = closing;
    }

    /**
     * Whether the last page is shown completely, so the screen can close.
     *
     * @return true if everything was read
     */
    public boolean isRead() {
        return !typing && !hasNextPage();
    }

    /**
     * Get what right-clicking does now.
     *
     * @return the hint
     */
    public Hint hint() {
        if (typing) {
            return Hint.SKIP;
        }
        if (hasNextPage() || closing && options.isEmpty()) {
            return Hint.CONTINUE;
        }
        return options.isEmpty() ? Hint.NONE : Hint.SELECT;
    }

    /**
     * Get the current screen and updates whether the page is still being typed.
     *
     * @return the screen
     */
    public HudScreen screen() {
        final Typewriter typewriter = new Typewriter(revealed);
        final List<Component> lines = new ArrayList<>();
        for (final Component line : pages.get(page.get())) {
            lines.add(typewriter.type(line));
        }
        typing = typewriter.isCut();
        while (lines.size() < lineCount) {
            lines.add(Component.empty());
        }
        return new HudScreen(lines, showsOptions() ? optionWindow() : List.of(), hint());
    }

    private List<Component> optionWindow() {
        final List<Component> lines = new ArrayList<>();
        int first = 0;
        int last = 0;
        for (int i = 0; i < options.size(); i++) {
            if (i == selected.get()) {
                first = lines.size();
                lines.addAll(selectedOptions.get(i));
                last = lines.size() - 1;
            } else {
                lines.addAll(options.get(i));
            }
        }
        if (first < optionTop) {
            optionTop = first;
        } else if (last >= optionTop + optionLineCount) {
            optionTop = last - optionLineCount + 1;
        }
        optionTop = Math.max(0, Math.min(optionTop, Math.max(0, lines.size() - optionLineCount)));
        final List<Component> window = new ArrayList<>(lines.subList(optionTop, Math.min(lines.size(), optionTop + optionLineCount)));
        while (window.size() < optionLineCount) {
            window.add(Component.empty());
        }
        return window;
    }

    /**
     * What right-clicking does now, shown as a hint on the HUD.
     */
    public enum Hint {
        /**
         * Nothing to do.
         */
        NONE,
        /**
         * Shows the rest of the page at once.
         */
        SKIP,
        /**
         * Turns to the next page, or closes the last one.
         */
        CONTINUE,
        /**
         * Selects the highlighted option, scrolling moves the selection.
         */
        SELECT
    }

    /**
     * A HUD conversation screen.
     *
     * @param lines   the lines of the dialogue box, padded to its line count
     * @param options the lines of the options box padded to its line count, or empty if the options are hidden
     * @param hint    what right-clicking does now
     */
    public record HudScreen(List<Component> lines, List<Component> options, Hint hint) {
    }
}
