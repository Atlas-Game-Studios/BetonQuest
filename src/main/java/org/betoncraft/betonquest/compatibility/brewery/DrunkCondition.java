package org.betoncraft.betonquest.compatibility.brewery;

import com.dre.brewery.BPlayer;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;

@SuppressWarnings("PMD.CommentRequired")
public class DrunkCondition extends Condition {

    private final Integer drunkness;

    public DrunkCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);

        drunkness = instruction.getInt();

        if (drunkness < 0 || drunkness > 100) {
            throw new InstructionParseException("Drunkness can only be between 0 and 100!");
        }
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final BPlayer bPlayer = BPlayer.get(PlayerConverter.getPlayer(playerID));
        return bPlayer != null && bPlayer.getDrunkeness() >= drunkness;
    }
}
