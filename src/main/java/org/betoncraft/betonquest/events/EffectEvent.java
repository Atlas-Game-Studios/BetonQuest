package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.VariableNumber;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;
import org.betoncraft.betonquest.utils.LogUtils;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.logging.Level;

/**
 * Gives the player specified potion effect
 */
@SuppressWarnings("PMD.CommentRequired")
public class EffectEvent extends QuestEvent {

    private final PotionEffectType effect;
    private final VariableNumber duration;
    private final VariableNumber amplifier;
    private final boolean ambient;
    private final boolean hidden;
    private final boolean icon;

    public EffectEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        final String type = instruction.next();
        effect = PotionEffectType.getByName(type);
        if (effect == null) {
            throw new InstructionParseException("Effect type '" + type + "' does not exist");
        }
        try {
            duration = instruction.getVarNum();
            amplifier = instruction.getVarNum();
        } catch (NumberFormatException e) {
            throw new InstructionParseException("Could not parse number arguments", e);
        }

        if (instruction.hasArgument("--ambient")) {
            LogUtils.getLogger().log(Level.WARNING, instruction.getID().getFullID() + ": Effect event uses \"--ambient\" which is deprecated. Please use \"ambient\"");
            ambient = true;
        } else {
            ambient = instruction.hasArgument("ambient");
        }

        hidden = instruction.hasArgument("hidden");
        icon = !instruction.hasArgument("noicon");
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        PlayerConverter.getPlayer(playerID).addPotionEffect(
                new PotionEffect(effect, duration.getInt(playerID) * 20, amplifier.getInt(playerID) - 1, ambient, !hidden, icon));
        return null;
    }

}
