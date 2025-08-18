package org.betonquest.betonquest.api.quest.npc;

import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.config.quest.QuestPackageManager;
import org.betonquest.betonquest.api.identifier.InstructionIdentifier;
import org.betonquest.betonquest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * Identifies a {@link org.betonquest.betonquest.api.quest.npc.Npc Npc} via the path syntax.
 */
public class NpcID extends InstructionIdentifier {

    /**
     * Creates a new Npc id.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package the ID is in
     * @param identifier  the id instruction string
     * @throws QuestException when the npc could not be resolved with the given identifier
     */
    public NpcID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "npcs", "Npc");
    }
}
