package org.betonquest.betonquest.compatibility.atlasitemregistry;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.item.QuestItemSerializer;
import org.bukkit.inventory.ItemStack;

/**
 * Serializes AtlasItemRegistry items into string form
 */
public class AtlasQuestItemSerializer implements QuestItemSerializer {

    /**
     * The empty default constructor.
     */
    public AtlasQuestItemSerializer() {
        // Empty
    }

    @Override
    public String serialize(final ItemStack itemStack) throws QuestException {
        return AtlasQuestItem.getMaterial(itemStack);
    }
}
