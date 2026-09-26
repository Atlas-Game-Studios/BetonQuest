package org.betonquest.betonquest.compatibility.atlasitemregistry;

import com.ags.atlasitemregistry.AtlasItemRegistryService;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.item.QuestItemWrapper;
import org.betonquest.betonquest.api.quest.TypeFactory;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

/**
 * Factory to create {@link AtlasQuestItem}s from {@link Instruction}s.
 */
public class AtlasQuestItemFactory implements TypeFactory<QuestItemWrapper> {

    /**
     * The AtlasItemRegistry service, null if not available.
     */
    @Nullable
    private final AtlasItemRegistryService registry;

    /**
     * Create a new factory, loading the AtlasItemRegistry service.
     */
    public AtlasQuestItemFactory() {
        registry = Bukkit.getServer().getServicesManager().load(AtlasItemRegistryService.class);
    }

    @Override
    public QuestItemWrapper parseInstruction(final Instruction instruction) throws QuestException {
        final Argument<String> material = instruction.string().get();
        if (registry == null) {
            throw new QuestException("AtlasItemRegistry service is not available");
        }
        return new AtlasQuestItemWrapper(registry, material);
    }
}
