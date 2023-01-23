package org.betoncraft.betonquest.compatibility.brewery;

import com.dre.brewery.BPlayer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;

@SuppressWarnings("PMD.CommentRequired")
public class DrunkQualityCondition extends Condition {

    private final Integer quality;

    public DrunkQualityCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);

        quality = instruction.getInt();

        if (quality <= 0 || quality > 10) {
            throw new InstructionParseException("Drunk quality can only be between 1 and 10!");
        }
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final BPlayer bPlayer = BPlayer.get(PlayerConverter.getPlayer(playerID));
        return bPlayer.getQuality() >= quality;
    }
}
