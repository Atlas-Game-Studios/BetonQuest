package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import com.ags.simplenpcs.NPCManager;
import com.ags.simplenpcs.SimpleNPCs;
import com.ags.simplenpcs.objects.SNPC;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.argument.Argument;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.quest.QuestException;
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
        final Variable<Number> npcId = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ZERO);
        return new SimpleWrapper(npcManager, npcId);
    }
}
