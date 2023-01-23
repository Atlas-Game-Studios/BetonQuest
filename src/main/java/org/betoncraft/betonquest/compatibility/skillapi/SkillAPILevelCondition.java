package org.betoncraft.betonquest.compatibility.skillapi;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.Condition;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.PlayerConverter;

import java.util.Optional;

/**
 * Checks the level of the player in SkillAPI.
 */
@SuppressWarnings("PMD.CommentRequired")
public class SkillAPILevelCondition extends Condition {

    private final String className;
    private final VariableNumber level;

    public SkillAPILevelCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        className = instruction.next();
        if (!SkillAPI.isClassRegistered(className)) {
            throw new InstructionParseException("Class '" + className + "' is not registered");
        }
        level = instruction.getVarNum();
    }

    @Override
    protected Boolean execute(final String playerID) throws QuestRuntimeException {
        final PlayerData data = SkillAPI.getPlayerData(PlayerConverter.getPlayer(playerID));
        final Optional<PlayerClass> playerClass = data
                .getClasses()
                .stream()
                .filter(c -> c.getData().getName().equalsIgnoreCase(className))
                .findAny();
        if (!playerClass.isPresent()) {
            return false;
        }
        return level.getInt(playerID) <= playerClass.get().getLevel();
    }

}
