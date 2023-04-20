package org.betonquest.betonquest.compatibility.simplenpcs;

import com.github.arnhav.SimpleNPCs;
import com.github.arnhav.api.NPCRightClickEvent;
import com.github.arnhav.objects.SNPC;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * Player has to right click the NPC
 */
@SuppressWarnings("PMD.CommentRequired")
public class NPCInteractObjective extends Objective implements Listener {

    private final int npcId;
    private final boolean cancel;

    public NPCInteractObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = ObjectiveData.class;
        npcId = instruction.getInt();
        if (npcId < 0) {
            throw new InstructionParseException("ID cannot be negative");
        }
        cancel = instruction.hasArgument("cancel");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onNPCClick(final NPCRightClickEvent event) {
        final OnlineProfile onlineProfile = PlayerConverter.getID(event.getPlayer());
        SNPC snpc = event.getNpc();
        if (snpc == null) return;
        int id = snpc.getId();
        if (id != npcId || !containsPlayer(onlineProfile)) {
            return;
        }
        if (checkConditions(onlineProfile)) {
            if (cancel) {
                event.setCancelled(true);
            }
            completeObjective(onlineProfile);
        }
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return "";
    }

    @Override
    public String getProperty(String name, Profile profile) {
        return "";
    }

}
