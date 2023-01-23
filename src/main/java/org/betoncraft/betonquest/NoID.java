package org.betoncraft.betonquest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.config.ConfigPackage;
import org.betoncraft.betonquest.exceptions.ObjectNotFoundException;

/**
 * @deprecated Use the {@link org.betoncraft.betonquest.id.NoID} instead,
 * this will be removed in 2.0 release
 */
// TODO Delete in BQ 2.0.0
@Deprecated
@SuppressWarnings({"PMD.ShortClassName", "PMD.CommentRequired"})
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class NoID extends org.betoncraft.betonquest.id.NoID {

    public NoID(final ConfigPackage pack) throws ObjectNotFoundException {
        super(pack);
    }

}
