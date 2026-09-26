package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import com.ags.simplenpcs.NPCManager;
import com.ags.simplenpcs.SimpleNPCs;
import com.ags.simplenpcs.objects.SNPC;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.quest.npc.NpcFactory;
import org.betonquest.betonquest.api.quest.npc.NpcWrapper;

/**
 * Factory to get FancyNpcs Npcs.
 */
public class SimpleFactory implements NpcFactory {
    /**
     * The empty default constructor.
     */
    public SimpleFactory() {
    }

    @Override
    public NpcWrapper<SNPC> parseInstruction(final Instruction instruction) throws QuestException {
        final NPCManager npcManager = SimpleNPCs.npcManager();
        final Argument<Number> npcId = instruction.number().atLeast(0).get();
        return new SimpleWrapper(npcManager, npcId);
    }
}
