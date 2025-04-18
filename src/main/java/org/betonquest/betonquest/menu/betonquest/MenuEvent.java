package org.betonquest.betonquest.menu.betonquest;

import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent;
import org.betonquest.betonquest.menu.MenuID;
import org.betonquest.betonquest.menu.RPGMenu;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Event to open or close menus.
 */
public class MenuEvent implements OnlineEvent {
    /**
     * The stuff to do with the profile.
     */
    private final Consumer<OnlineProfile> doStuff;

    /**
     * Creates a new MenuQuestEvent.
     *
     * @param rpgMenu the rpg menu instance to open and close menus
     * @param menuID  the menu id to open or null if open menus should be closed
     */
    public MenuEvent(final RPGMenu rpgMenu, @Nullable final MenuID menuID) {
        if (menuID != null) {
            doStuff = profile -> rpgMenu.openMenu(profile, menuID);
        } else {
            doStuff = RPGMenu::closeMenu;
        }
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        doStuff.accept(profile);
    }
}
