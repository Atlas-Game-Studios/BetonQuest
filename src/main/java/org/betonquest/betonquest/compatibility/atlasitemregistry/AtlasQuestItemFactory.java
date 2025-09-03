package org.betonquest.betonquest.compatibility.atlasitemregistry;

import com.ags.atlasitemregistry.AtlasItemRegistryService;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.argument.Argument;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.item.QuestItemWrapper;
import org.betonquest.betonquest.kernel.registry.TypeFactory;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

/**
 * Factory to create {@link AtlasQuestItem}s from {@link Instruction}s.
 */
public class AtlasQuestItemFactory implements TypeFactory<QuestItemWrapper> {

    private final AtlasItemRegistryService registry;

    public AtlasQuestItemFactory() {
        registry = Bukkit.getServer().getServicesManager().load(AtlasItemRegistryService.class);
    }

    @Override
    public QuestItemWrapper parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<String> material = instruction.get(Argument.STRING);
        assert registry != null;
        return new AtlasQuestItemWrapper(registry, material);
    }
}
