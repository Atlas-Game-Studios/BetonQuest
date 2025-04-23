package org.betonquest.betonquest.quest.event.spawn;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.instruction.Item;
import org.betonquest.betonquest.instruction.variable.VariableList;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for the equipment and drops of a mob.
 *
 * @param helmet     the helmet to equip the mob with
 * @param chestplate the chest plate to equip the mob with
 * @param leggings   the leggings to equip the mob with
 * @param boots      the boots to equip the mob with
 * @param mainHand   the main hand item to equip the mob with
 * @param offHand    the off-hand item to equip the mob with
 * @param drops      the items to drop when the mob dies
 */
public record Equipment(@Nullable Item helmet, @Nullable Item chestplate,
                        @Nullable Item leggings, @Nullable Item boots, @Nullable Item mainHand,
                        @Nullable Item offHand, VariableList<Item> drops) {

    /**
     * Adds the drops to the mob.
     *
     * @param mob     the mob to add the drops to
     * @param profile the profile to get the drop amounts from
     * @throws QuestException if the variable could not be resolved
     */
    public void addDrops(final Mob mob, @Nullable final Profile profile) throws QuestException {
        int dropIndex = 0;
        for (final Item item : drops.getValue(profile)) {
            final String value = item.getID().getFullID() + ":" + item.getAmount().getValue(profile).intValue();
            final NamespacedKey key = new NamespacedKey(BetonQuest.getInstance(), "betonquest-drops-" + dropIndex);
            mob.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
            dropIndex++;
        }
    }

    /**
     * Adds the equipment to the mob and sets the drop chances to 0 for the equipment.
     *
     * @param mob the mob to add the equipment to
     * @throws QuestException if a QuestItem does not exist
     */
    public void addEquipment(final Mob mob) throws QuestException {
        final EntityEquipment equipment = mob.getEquipment();
        equipment.setHelmet(generate(helmet));
        equipment.setChestplate(generate(chestplate));
        equipment.setLeggings(generate(leggings));
        equipment.setBoots(generate(boots));
        equipment.setItemInMainHand(generate(mainHand));
        equipment.setItemInOffHand(generate(offHand));
        equipment.setHelmetDropChance(0);
        equipment.setChestplateDropChance(0);
        equipment.setLeggingsDropChance(0);
        equipment.setBootsDropChance(0);
        equipment.setItemInMainHandDropChance(0);
        equipment.setItemInOffHandDropChance(0);
    }

    @Nullable
    private ItemStack generate(@Nullable final Item item) throws QuestException {
        return item == null ? null : item.getItem().generate(1);
    }
}
