package org.betonquest.betonquest.compatibility.atlasitemregistry;

import com.ags.atlasitemregistry.AtlasItemRegistryService;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.item.QuestItem;
import org.betonquest.betonquest.api.item.QuestItemWrapper;
import org.betonquest.betonquest.api.profile.Profile;
import org.jetbrains.annotations.Nullable;

/**
 * Creates {@link AtlasQuestItem}s from material.
 */
public class AtlasQuestItemWrapper implements QuestItemWrapper {

    /**
     * The AtlasItemRegistry service.
     */
    private final AtlasItemRegistryService registry;

    /**
     * The registry material of the item.
     */
    private final Argument<String> material;

    /**
     * Create a new wrapper for an AtlasItemRegistry item.
     *
     * @param registry the AtlasItemRegistry service
     * @param material the registry material of the item
     */
    public AtlasQuestItemWrapper(final AtlasItemRegistryService registry, final Argument<String> material) {
        this.registry = registry;
        this.material = material;
    }

    @Override
    public QuestItem getItem(@Nullable final Profile profile) throws QuestException {
        final String mat = material.getValue(profile);
        return new AtlasQuestItem(registry.getItem(mat), mat);
    }
}
