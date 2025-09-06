package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import com.ags.simplenpcs.objects.SNPC;
import org.betonquest.betonquest.compatibility.npc.GenericReverseIdentifier;

/**
 * Allows to get NpcIds for a SimpleNPCs Npc.
 */
public class SimpleIdentifier extends GenericReverseIdentifier<SNPC> {

    /**
     * Create a new Simple Identifier.
     *
     * @param prefix the prefix of relevant Ids
     */
    public SimpleIdentifier(final String prefix) {
        super(prefix.toLowerCase(), SNPC.class, original -> String.valueOf(original.getId()));
    }
}
