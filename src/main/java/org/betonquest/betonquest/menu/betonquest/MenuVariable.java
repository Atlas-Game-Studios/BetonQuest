package org.betonquest.betonquest.menu.betonquest;

import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.variable.online.OnlineVariable;
import org.betonquest.betonquest.menu.OpenedMenu;

/**
 * Returns the title of the players currently opened menu.
 */
public class MenuVariable implements OnlineVariable {

    /**
     * Create a new opened menu variable.
     */
    public MenuVariable() {
    }

    @Override
    public String getValue(final OnlineProfile profile) {
        final OpenedMenu menu = OpenedMenu.getMenu(profile);
        if (menu == null) {
            return "";
        }
        return menu.getData().getTitle(profile);
    }
}
