package org.betoncraft.betonquest.variables;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Variable;
import org.betoncraft.betonquest.config.Config;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.id.ConditionID;

@SuppressWarnings("PMD.CommentRequired")
public class ConditionVariable extends Variable {

    private final ConditionID conditionId;
    private final boolean papiMode;

    public ConditionVariable(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        conditionId = instruction.getCondition();
        papiMode = instruction.hasArgument("papiMode");
    }

    @Override
    public String getValue(final String playerID) {
        final String lang = BetonQuest.getInstance().getPlayerData(playerID).getLanguage();

        if (BetonQuest.condition(playerID, conditionId)) {
            return papiMode ? Config.getMessage(lang, "condition_variable_met") : "true";
        }
        return papiMode ? Config.getMessage(lang, "condition_variable_not_met") : "false";
    }
}
