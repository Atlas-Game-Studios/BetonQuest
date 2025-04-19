package org.betonquest.betonquest.quest.objective.tame;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.argument.VariableArgument;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Tameable;

/**
 * Factory for creating {@link TameObjective} instances from {@link Instruction}s.
 */
public class TameObjectiveFactory implements ObjectiveFactory {
    /**
     * Creates a new TameObjectiveFactory instance.
     */
    public TameObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final EntityType type = instruction.getEnum(EntityType.class);
        if (type.getEntityClass() == null || !Tameable.class.isAssignableFrom(type.getEntityClass())) {
            throw new QuestException("Entity cannot be tamed: " + type);
        }
        final VariableNumber targetAmount = instruction.get(VariableArgument.NUMBER_NOT_LESS_THAN_ONE);
        return new TameObjective(instruction, targetAmount, type);
    }
}
