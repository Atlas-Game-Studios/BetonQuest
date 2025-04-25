package org.betonquest.betonquest.compatibility.mmogroup.mmocore.condition;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.condition.PlayerCondition;
import org.betonquest.betonquest.instruction.variable.Variable;

/**
 * Condition to check if the player has a level in an attribute.
 */
public class MMOCoreAttributeCondition implements PlayerCondition {

    /**
     * Attribute to check.
     */
    private final PlayerAttribute attribute;

    /**
     * If the actual must be equal to the target level.
     */
    private final boolean mustBeEqual;

    /**
     * Required level.
     */
    private final Variable<Number> targetLevelVar;

    /**
     * Create a new attribute condition.
     *
     * @param attribute   the attribute to check
     * @param targetLevel the required level
     * @param equal       whether the actual must be equal to the target level
     */
    public MMOCoreAttributeCondition(final PlayerAttribute attribute, final Variable<Number> targetLevel, final boolean equal) {
        this.attribute = attribute;
        this.targetLevelVar = targetLevel;
        this.mustBeEqual = equal;
    }

    @Override
    public boolean check(final Profile profile) throws QuestException {
        final int targetLevel = targetLevelVar.getValue(profile).intValue();
        final int actualLevel = PlayerData.get(profile.getPlayerUUID()).getAttributes().getAttribute(attribute);

        return mustBeEqual ? actualLevel == targetLevel : actualLevel >= targetLevel;
    }
}
