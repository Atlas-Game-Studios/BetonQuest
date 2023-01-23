package org.betoncraft.betonquest.id;

import org.betoncraft.betonquest.config.ConfigPackage;
import org.betoncraft.betonquest.exceptions.ObjectNotFoundException;

@SuppressWarnings("PMD.CommentRequired")
public class ItemID extends ID {

    public ItemID(final ConfigPackage pack, final String identifier) throws ObjectNotFoundException {
        super(pack, identifier);
        rawInstruction = super.pack.getString("items." + super.identifier);
        if (rawInstruction == null) {
            throw new ObjectNotFoundException("Item '" + getFullID() + "' is not defined");
        }
    }

}
