package org.betoncraft.betonquest.id;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableInstruction;
import org.betoncraft.betonquest.config.ConfigPackage;
import org.betoncraft.betonquest.exceptions.ObjectNotFoundException;

@SuppressWarnings("PMD.CommentRequired")
public class GlobalVariableID extends ID {
    public GlobalVariableID(final ConfigPackage pack, final String identifier) throws ObjectNotFoundException {
        super(pack, identifier);
    }

    @Override
    public Instruction generateInstruction() {
        return new VariableInstruction(pack, this, identifier);
    }

    @Override
    public String getFullID() {
        return pack.getName() + "-" + getBaseID();
    }

}
