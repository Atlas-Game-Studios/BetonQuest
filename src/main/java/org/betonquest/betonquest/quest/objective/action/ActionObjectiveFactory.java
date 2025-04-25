package org.betonquest.betonquest.quest.objective.action;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.argument.Argument;
import org.betonquest.betonquest.instruction.variable.Variable;
import org.betonquest.betonquest.util.BlockSelector;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Factory class for creating {@link ActionObjective} instances from {@link Instruction}s.
 */
public class ActionObjectiveFactory implements ObjectiveFactory {
    /**
     * The "any" keyword.
     */
    private static final String ANY = "any";

    /**
     * Logger factory to create a logger for the objectives.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Creates a new instance of the ActionObjectiveFactory.
     *
     * @param loggerFactory the logger factory to create a logger for the objectives
     */
    public ActionObjectiveFactory(final BetonQuestLoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Click action = instruction.getEnum(Click.class);
        final Variable<BlockSelector> selector;
        if (ANY.equalsIgnoreCase(instruction.next())) {
            selector = null;
        } else {
            selector = instruction.getVariable(instruction.current(), Argument.BLOCK_SELECTOR);
        }
        final boolean exactMatch = instruction.hasArgument("exactMatch");
        final Variable<Location> loc = instruction.getVariable(instruction.getOptional("loc"), Argument.LOCATION);
        final Variable<Number> range = instruction.getVariable(instruction.getOptional("range", "0"), Argument.NUMBER);
        final boolean cancel = instruction.hasArgument("cancel");
        final String handString = instruction.getOptional("hand");
        final EquipmentSlot slot;
        if (handString == null || handString.equalsIgnoreCase(EquipmentSlot.HAND.toString())) {
            slot = EquipmentSlot.HAND;
        } else if (handString.equalsIgnoreCase(EquipmentSlot.OFF_HAND.toString())) {
            slot = EquipmentSlot.OFF_HAND;
        } else if (ANY.equalsIgnoreCase(handString)) {
            slot = null;
        } else {
            throw new QuestException("Invalid hand value: " + handString);
        }
        final BetonQuestLogger log = loggerFactory.create(ActionObjective.class);
        return new ActionObjective(instruction, log, action, selector, exactMatch, loc, range, cancel, slot);
    }
}
