package org.betonquest.betonquest.quest.event.spawn;

import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.PlayerEvent;
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory;
import org.betonquest.betonquest.api.quest.event.PlayerlessEvent;
import org.betonquest.betonquest.api.quest.event.PlayerlessEventFactory;
import org.betonquest.betonquest.api.quest.event.nullable.NullableEventAdapter;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.Item;
import org.betonquest.betonquest.instruction.argument.Argument;
import org.betonquest.betonquest.instruction.variable.Variable;
import org.betonquest.betonquest.instruction.variable.VariableIdentifier;
import org.betonquest.betonquest.instruction.variable.VariableList;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.betonquest.betonquest.instruction.variable.VariableString;
import org.betonquest.betonquest.quest.PrimaryServerThreadData;
import org.betonquest.betonquest.quest.event.PrimaryServerThreadEvent;
import org.betonquest.betonquest.quest.event.PrimaryServerThreadPlayerlessEvent;
import org.betonquest.betonquest.util.Utils;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Factory to create spawn mob events from {@link Instruction}s.
 */
public class SpawnMobEventFactory implements PlayerEventFactory, PlayerlessEventFactory {
    /**
     * Data used for primary server access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create a new factory for {@link SpawnMobEvent}s.
     *
     * @param data the primary server thread data required for main thread checking
     */
    public SpawnMobEventFactory(final PrimaryServerThreadData data) {
        this.data = data;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadEvent(createSpawnMobEvent(instruction), data);
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadPlayerlessEvent(createSpawnMobEvent(instruction), data);
    }

    /**
     * Creates a new spawn mob event from the given instruction.
     *
     * @param instruction the instruction to create the event from
     * @return the created event
     * @throws QuestException if the instruction could not be parsed
     */
    public NullableEventAdapter createSpawnMobEvent(final Instruction instruction) throws QuestException {
        final Variable<Location> loc = instruction.getVariable(Argument.LOCATION);
        final EntityType type = instruction.getEnum(EntityType.class);
        final VariableNumber amount = instruction.get(VariableNumber::new);
        final String nameString = instruction.getOptional("name");
        final VariableString name = nameString == null ? null : instruction.get(Utils.format(
                nameString, true, false).replace('_', ' '), VariableString::new);
        final VariableIdentifier marked = instruction.get(instruction.getOptional("marked"), VariableIdentifier::new);
        final Item helmet = getItem(instruction, "h");
        final Item chestplate = getItem(instruction, "c");
        final Item leggings = getItem(instruction, "l");
        final Item boots = getItem(instruction, "b");
        final Item mainHand = getItem(instruction, "m");
        final Item offHand = getItem(instruction, "o");
        final VariableList<Item> drops = instruction.getItemList(instruction.getOptional("drops"));
        final Equipment equipment = new Equipment(helmet, chestplate, leggings, boots, mainHand, offHand, drops);
        final SpawnMobEvent event = new SpawnMobEvent(loc, type, equipment, amount, name, marked);
        return new NullableEventAdapter(event);
    }

    @Nullable
    private Item getItem(final Instruction instruction, final String key) throws QuestException {
        return instruction.getItem(instruction.getOptional(key));
    }
}
