package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.id.ConditionID;
import org.betoncraft.betonquest.id.EventID;
import org.betoncraft.betonquest.utils.Utils;

import java.util.List;

/**
 * Fires specified events for every player in the party
 */
@SuppressWarnings("PMD.CommentRequired")
public class PartyEvent extends QuestEvent {

    private final ConditionID[] conditions;
    private final EventID[] events;
    private final VariableNumber range;

    public PartyEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        range = instruction.getVarNum();
        conditions = instruction.getList(e -> instruction.getCondition(e)).toArray(new ConditionID[0]);
        events = instruction.getList(e -> instruction.getEvent(e)).toArray(new EventID[0]);
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        final List<String> members = Utils.getParty(playerID, range.getDouble(playerID), instruction.getPackage()
                .getName(), conditions);
        for (final String memberID : members) {
            for (final EventID event : events) {
                BetonQuest.event(memberID, event);
            }
        }
        return null;
    }

}
