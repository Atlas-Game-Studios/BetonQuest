package org.betonquest.betonquest.compatibility.atlasitemregistry;

import com.ags.atlasitemregistry.AtlasItemRegistryService;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.item.QuestItem;
import org.betonquest.betonquest.item.QuestItemWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Creates {@link AtlasQuestItem}s from material.
 */
public class AtlasQuestItemWrapper implements QuestItemWrapper {

    private final AtlasItemRegistryService registry;

    private final Variable<String> material;

    public AtlasQuestItemWrapper(final AtlasItemRegistryService registry, final Variable<String> material) {
        this.registry = registry;
        this.material = material;
    }

    @Override
    public QuestItem getItem(@Nullable final Profile profile) throws QuestException {
        final String mat = material.getValue(profile);
        return new AtlasQuestItem(registry.getItem(mat), mat);
    }
}
