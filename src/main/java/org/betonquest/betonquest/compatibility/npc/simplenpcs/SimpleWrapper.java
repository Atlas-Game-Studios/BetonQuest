package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import com.ags.simplenpcs.NPCManager;
import com.ags.simplenpcs.objects.SNPC;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.npc.NpcWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * FancyNpcs wrapper to get a Npc.
 */
public class SimpleWrapper implements NpcWrapper<SNPC> {

    /**
     * FancyNpcs Npc Manager.
     */
    private final NPCManager npcManager;

    /**
     * Npc identifier.
     */
    private final Variable<Number> npcId;

    /**
     * Create a new FancyNpcs Npc Wrapper.
     *
     * @param npcManager the Npc Manager to get Npcs from
     * @param npcId      the npc identifier
     */
    public SimpleWrapper(final NPCManager npcManager, final Variable<Number> npcId) {
        this.npcManager = npcManager;
        this.npcId = npcId;
    }

    @Override
    public org.betonquest.betonquest.api.quest.npc.Npc<SNPC> getNpc(@Nullable final Profile profile) throws QuestException {
        final SNPC npc;
        final int npcId = this.npcId.getValue(profile).intValue();
        npc = npcManager.getNPC(npcId);
        if (npc == null) {
            throw new QuestException("Simple Npc with id " + npcId + " not found");
        }
        return new SimpleAdapter(npc);
    }
}
