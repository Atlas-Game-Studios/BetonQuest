package org.betonquest.betonquest.compatibility.simplemagic;

import com.mc_atlas.simplemagic.api.SimpleMagicService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("PMD.CommentRequired")
public class HasDudCondition extends Condition {

    private final Integer count;

    private final SimpleMagicService simpleMagic;

    public HasDudCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);

        count = instruction.getInt();
        if (count <= 0) {
            throw new InstructionParseException("Can't give less than one spellbook!");
        }

        simpleMagic = BetonQuest.simpleMagic();
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    protected Boolean execute(final Profile profile) throws QuestRuntimeException {
        final Player player = profile.getOnlineProfile().get().getPlayer();

        int remaining = count;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            final ItemStack item = player.getInventory().getItem(i);
            if (item != null && simpleMagic.isSpellBook(item) && simpleMagic.isDudSpellBook(item)) {

                remaining -= item.getAmount();
                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }
}
