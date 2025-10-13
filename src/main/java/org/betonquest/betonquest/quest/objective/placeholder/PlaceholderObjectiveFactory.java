package org.betonquest.betonquest.quest.objective.placeholder;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory;

public class PlaceholderObjectiveFactory implements ObjectiveFactory {
    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        return new PlaceholderObjective(instruction);
    }
}
