package org.betonquest.betonquest.quest.objective.placeholder;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;

public class PlaceholderObjective extends Objective {

    public PlaceholderObjective(final Instruction instruction) throws QuestException {
        super(instruction);
    }

    @Override
    public String getDefaultDataInstruction() {
        return "";
    }

    @Override
    public String getProperty(final String name, final Profile profile) throws QuestException {
        return "";
    }
}
