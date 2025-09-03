package org.betonquest.betonquest.compatibility.atlasitemregistry;

import com.ags.atlasitemregistry.atlaslib.pdc.DataType;
import com.ags.atlasitemregistry.atlaslib.util.PDC;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.item.QuestItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Quest Item implementation for AtlasItemRegistry.
 */
public class AtlasQuestItem implements QuestItem {

    private final ItemStack resolvedItem;

    private final String material;

    public AtlasQuestItem(final ItemStack resolvedItem, final String material) {
        this.resolvedItem = resolvedItem;
        this.material = material;
    }

    @Override
    public String getName() {
        final ItemMeta itemMeta = resolvedItem.getItemMeta();
        return itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : material;
    }

    @Override
    public List<String> getLore() {
        final ItemMeta itemMeta = resolvedItem.getItemMeta();
        return itemMeta.hasLore() ? itemMeta.getLore() : List.of();
    }

    @Override
    public ItemStack generate(final int stackSize, @Nullable final Profile profile) {
        return resolvedItem.clone();
    }

    @Override
    public boolean matches(@Nullable final ItemStack item) {
        if (item == null) return false;
        if (PDC.has(item, "material")) return material.equals(PDC.get(item, "material", DataType.STRING));
        return material.equals(item.getType().toString());
    }
}
