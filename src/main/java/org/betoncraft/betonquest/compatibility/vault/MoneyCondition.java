package org.betoncraft.betonquest.compatibility.vault;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Checks if the player has specified amount of Vault money
 */
@SuppressWarnings("PMD.CommentRequired")
public class MoneyCondition extends Condition {

    private final VariableNumber amount;

    public MoneyCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        amount = instruction.getVarNum();
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        double pAmount = amount.getDouble(playerID);
        if (pAmount < 0) {
            pAmount = -pAmount;
        }
        return VaultIntegrator.getEconomy().has(PlayerConverter.getPlayer(playerID), pAmount);
    }

}
