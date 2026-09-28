package org.betonquest.betonquest.conversation.menu.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuts components down to a character budget, keeping their style.
 * Non-text components like translatables count as one character.
 */
final class Typewriter {

    /**
     * Characters left to reveal.
     */
    private int budget;

    /**
     * If any text was cut off.
     */
    private boolean cut;

    /**
     * Creates a new typewriter.
     *
     * @param budget the number of characters to reveal
     */
    /* default */ Typewriter(final int budget) {
        this.budget = budget;
    }

    /**
     * Whether any text was cut off so far.
     *
     * @return true if text was cut off
     */
    /* default */ boolean isCut() {
        return cut;
    }

    /**
     * Reveals the component as far as the remaining budget allows.
     *
     * @param component the component to reveal
     * @return the revealed part
     */
    /* default */ Component type(final Component component) {
        if (cut) {
            return Component.empty();
        }
        Component result = component;
        if (component instanceof final TextComponent text) {
            final String content = text.content();
            final int length = content.codePointCount(0, content.length());
            if (length > budget) {
                cut = true;
                result = text.content(content.substring(0, content.offsetByCodePoints(0, budget)));
                budget = 0;
                return result.children(List.of());
            }
            budget -= length;
        } else if (budget > 0) {
            budget--;
        } else {
            cut = true;
            return Component.empty();
        }
        final List<Component> children = new ArrayList<>();
        for (final Component child : component.children()) {
            children.add(type(child));
            if (cut) {
                break;
            }
        }
        return result.children(children);
    }
}
