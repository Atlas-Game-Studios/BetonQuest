package org.betoncraft.betonquest.commands;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.Backpack;
import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.utils.LogUtils;
import org.betoncraft.betonquest.utils.PlayerConverter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * The backpack command. It opens player's backpack.
 */
public class BackpackCommand implements CommandExecutor {

    /**
     * Registers a new executor of the /backpack command
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public BackpackCommand() {
        BetonQuest.getInstance().getCommand("backpack").setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if ("backpack".equalsIgnoreCase(cmd.getName())) {
            // command sender must be a player, console can't have a backpack
            if (sender instanceof Player) {
                LogUtils.getLogger().log(Level.FINE, "Executing /backpack command for " + sender.getName());
                new Backpack(PlayerConverter.getID((Player) sender));
            }
            return true;
        }
        return false;
    }
}
