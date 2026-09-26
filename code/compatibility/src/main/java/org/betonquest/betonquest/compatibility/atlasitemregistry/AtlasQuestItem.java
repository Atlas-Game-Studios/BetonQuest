package org.betonquest.betonquest.compatibility.atlasitemregistry;

import com.ags.atlasitemregistry.atlaslib.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.api.item.QuestItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Quest Item implementation for AtlasItemRegistry.
 */
public class AtlasQuestItem implements QuestItem {

    /**
     * The key AtlasItemRegistry stores the registry material under.
     */
    private static final NamespacedKey MATERIAL_KEY = new NamespacedKey("atlas", "material");

    /**
     * The item resolved from the registry.
     */
    private final ItemStack resolvedItem;

    /**
     * The registry material of the item.
     */
    private final String material;

    /**
     * Create a new AtlasItemRegistry quest item.
     *
     * @param resolvedItem the item resolved from the registry
     * @param material     the registry material of the item
     */
    public AtlasQuestItem(final ItemStack resolvedItem, final String material) {
        this.resolvedItem = resolvedItem;
        this.material = material;
    }

    @Override
    public Component getName() {
        final ItemMeta itemMeta = resolvedItem.getItemMeta();
        return itemMeta.hasDisplayName() ? itemMeta.displayName() : MessageUtil.convertMsg(material);
    }

    @Override
    public List<Component> getLore() {
        final ItemMeta itemMeta = resolvedItem.getItemMeta();
        return itemMeta.hasLore() ? itemMeta.lore() : List.of();
    }

    @Override
    public ItemStack generate(final int stackSize) {
        return resolvedItem.clone();
    }

    @Override
    public boolean matches(@Nullable final ItemStack item) {
        return item != null && material.equals(getMaterial(item));
    }

    /**
     * Gets the registry material of an item, falling back to its vanilla type.
     *
     * @param item the item
     * @return the registry material or the vanilla type name
     */
    /* default */ static String getMaterial(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        final String material = meta == null ? null : meta.getPersistentDataContainer().get(MATERIAL_KEY, PersistentDataType.STRING);
        return material == null ? item.getType().name() : material;
    }
}
