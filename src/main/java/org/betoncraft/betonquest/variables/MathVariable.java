package org.betoncraft.betonquest.variables;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Variable;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.LogUtils;
import org.betoncraft.betonquest.utils.math.Tokenizer;
import org.betoncraft.betonquest.utils.math.tokens.Token;

import java.util.Locale;
import java.util.logging.Level;

/**
 * This variable evaluates the given calculation and returns the result.
 */
@SuppressWarnings({"PMD.CommentRequired", "deprecation"})
public class MathVariable extends Variable {

    private final Token calculation;

    public MathVariable(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        final String instructionString = instruction.getInstruction();
        if (!instructionString.matches("math\\.calc:.+")) {
            throw new InstructionParseException("invalid format");
        }
        final String expression = instructionString.substring("math.calc:".length());
        this.calculation = new Tokenizer(instruction.getPackage().getName()).tokenize(expression);
    }

    @Override
    public String getValue(final String playerID) {
        try {
            final double value = calculation.resolve(playerID);
            if (value % 1 == 0) {
                return String.format(Locale.US, "%.0f", value);
            }
            return String.valueOf(value);
        } catch (QuestRuntimeException e) {
            LogUtils.getLogger().log(Level.WARNING, "Could not calculate '" + calculation + "' (" + e.getMessage() + "). Returning 0 instead.");
            LogUtils.logThrowable(e);
            return "0";
        }
    }
}
