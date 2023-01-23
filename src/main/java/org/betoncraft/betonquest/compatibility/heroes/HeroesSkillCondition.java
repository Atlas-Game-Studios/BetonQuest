package org.betoncraft.betonquest.compatibility.heroes;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Checks if the player has access to specified Heroes skill.
 */
@SuppressWarnings("PMD.CommentRequired")
public class HeroesSkillCondition extends Condition {

    private final String skillName;

    public HeroesSkillCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        skillName = instruction.next();
    }

    @Override
    protected Boolean execute(final String playerID) {
        final Hero hero = Heroes.getInstance().getCharacterManager().getHero(PlayerConverter.getPlayer(playerID));
        if (hero == null) {
            return false;
        }
        return hero.canUseSkill(skillName);
    }

}
