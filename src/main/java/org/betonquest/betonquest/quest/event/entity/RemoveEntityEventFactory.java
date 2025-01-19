package org.betonquest.betonquest.quest.event.entity;

import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.api.quest.event.EventFactory;
import org.betonquest.betonquest.api.quest.event.StaticEvent;
import org.betonquest.betonquest.api.quest.event.StaticEventFactory;
import org.betonquest.betonquest.api.quest.event.nullable.NullableEventAdapter;
import org.betonquest.betonquest.exception.QuestException;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.argument.VariableArgument;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.betonquest.betonquest.instruction.variable.VariableString;
import org.betonquest.betonquest.instruction.variable.location.VariableLocation;
import org.betonquest.betonquest.quest.PrimaryServerThreadData;
import org.betonquest.betonquest.quest.event.PrimaryServerThreadEvent;
import org.betonquest.betonquest.quest.event.PrimaryServerThreadStaticEvent;
import org.betonquest.betonquest.util.Utils;
import org.bukkit.entity.EntityType;

import java.util.Locale;

/**
 * Factory for {@link RemoveEntityEvent} to create from {@link Instruction}.
 */
public class RemoveEntityEventFactory implements EventFactory, StaticEventFactory {

    /**
     * Data for primary server thread access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Creates a new KillMobEventFactory.
     *
     * @param data the data for primary server thread access
     */
    public RemoveEntityEventFactory(final PrimaryServerThreadData data) {
        this.data = data;
    }

    @Override
    public Event parseEvent(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadEvent(createRemoveEntityEvent(instruction), data);
    }

    @Override
    public StaticEvent parseStaticEvent(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadStaticEvent(createRemoveEntityEvent(instruction), data);
    }

    private NullableEventAdapter createRemoveEntityEvent(final Instruction instruction) throws QuestException {
        final String[] entities = instruction.getArray();
        final EntityType[] types = new EntityType[entities.length];
        for (int i = 0; i < types.length; i++) {
            try {
                types[i] = EntityType.valueOf(entities[i].toUpperCase(Locale.ROOT));
            } catch (final IllegalArgumentException e) {
                throw new QuestException("Entity type '" + entities[i] + "' does not exist", e);
            }
        }
        final VariableLocation loc = instruction.get(VariableLocation::new);
        final VariableNumber range = instruction.get(VariableNumber::new);
        final boolean kill = instruction.hasArgument("kill");
        final String nameString = instruction.getOptional("name");
        final VariableString name = nameString == null ? null : instruction.get(
                Utils.format(nameString, true, false).replace('_', ' '), VariableString::new);
        final String markedString = instruction.getOptional("marked");
        final VariableString marked = markedString == null ? null : instruction.get(markedString, VariableArgument.STRING_WITH_PACKAGE);
        return new NullableEventAdapter(new RemoveEntityEvent(types, loc, range, name, marked, kill));
    }
}
