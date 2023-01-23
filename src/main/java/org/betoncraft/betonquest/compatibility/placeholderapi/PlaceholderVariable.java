package org.betoncraft.betonquest.compatibility.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Variable;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;

@SuppressWarnings("PMD.CommentRequired")
public class PlaceholderVariable extends Variable {

    private final String placeholder;

    public PlaceholderVariable(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        placeholder = instruction.getInstruction().substring(3);
    }

    @Override
    public String getValue(final String playerID) {
        return PlaceholderAPI.setPlaceholders(PlayerConverter.getPlayer(playerID), '%' + placeholder + '%');
    }

}
