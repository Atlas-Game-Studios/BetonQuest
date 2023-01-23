package org.betoncraft.betonquest.id;

import org.betoncraft.betonquest.config.ConfigPackage;
import org.betoncraft.betonquest.exceptions.ObjectNotFoundException;

@SuppressWarnings({"PMD.ShortClassName", "PMD.CommentRequired"})
public class NoID extends ID {

    public NoID(final ConfigPackage pack) throws ObjectNotFoundException {
        super(pack, "no-id");
    }

}
