package org.betoncraft.betonquest.compatibility.vault;

import net.milkbowl.vault.permission.Permission;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.entity.Player;

/**
 * Manages player's permissions
 */
@SuppressWarnings("PMD.CommentRequired")
public class PermissionEvent extends QuestEvent {

    private final String world;
    private final String permission;
    private final boolean add;
    private final boolean perm;

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public PermissionEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        add = "add".equalsIgnoreCase(instruction.next());
        perm = "perm".equalsIgnoreCase(instruction.next());
        permission = instruction.next();
        if (instruction.size() >= 5) {
            world = instruction.next();
        } else {
            world = null;
        }
    }

    @Override
    protected Void execute(final String playerID) {
        final Permission vault = VaultIntegrator.getPermission();
        final Player player = PlayerConverter.getPlayer(playerID);
        if (add) {
            if (perm) {
                vault.playerAdd(world, player, permission);
            } else {
                vault.playerAddGroup(world, player, permission);
            }
        } else {
            if (perm) {
                vault.playerRemove(world, player, permission);
            } else {
                vault.playerRemoveGroup(world, player, permission);
            }
        }
        return null;
    }
}
