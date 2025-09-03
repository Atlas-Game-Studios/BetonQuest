package org.betonquest.betonquest.compatibility.atlasitemregistry;

import com.ags.atlasitemregistry.atlaslib.pdc.DataType;
import com.ags.atlasitemregistry.atlaslib.util.PDC;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.item.QuestItemSerializer;
import org.bukkit.inventory.ItemStack;

/**
 * Serializes AtlasItemRegistry items into string form
 */
public class AtlasQuestItemSerializer implements QuestItemSerializer {

    @Override
    public String serialize(final ItemStack itemStack) throws QuestException {
        String material = itemStack.getType().name();
        if (PDC.has(itemStack, "material")) material = PDC.get(itemStack, "material", DataType.STRING);
        return material;
    }
}
