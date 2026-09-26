package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import com.ags.simplenpcs.api.NPCLeftClickEvent;
import com.ags.simplenpcs.api.NPCRightClickEvent;
import com.ags.simplenpcs.objects.SNPC;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.api.quest.npc.NpcInteractCatcher;
import org.betonquest.betonquest.api.service.npc.NpcRegistry;
import org.betonquest.betonquest.quest.objective.interact.Interaction;
import org.bukkit.event.EventHandler;

/**
 * Catches interaction with FancyNpcs.
 */
public class SimpleCatcher extends NpcInteractCatcher<SNPC> {
    /**
     * Initializes the Fancy catcher.
     *
     * @param profileProvider the profile provider instance
     * @param npcRegistry     the registry to identify the clicked Npc
     */
    public SimpleCatcher(final ProfileProvider profileProvider, final NpcRegistry npcRegistry) {
        super(profileProvider, npcRegistry);
    }

    /**
     * Catches clicks.
     *
     * @param event the Interact Event
     */
    @EventHandler(ignoreCancelled = true)
    public void onRightClick(final NPCRightClickEvent event) {
        if (interactLogic(event.getPlayer(), new SimpleAdapter(event.getNpc()), Interaction.RIGHT, false, event.isAsynchronous())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles left clicks on SimpleNPCs.
     *
     * @param event the left click event
     */
    @EventHandler(ignoreCancelled = true)
    public void onLeftClick(final NPCLeftClickEvent event) {
        if (interactLogic(event.getPlayer(), new SimpleAdapter(event.getNpc()), Interaction.LEFT, false, event.isAsynchronous())) {
            event.setCancelled(true);
        }
    }
}
