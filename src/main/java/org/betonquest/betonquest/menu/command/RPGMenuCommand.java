package org.betonquest.betonquest.menu.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.config.PluginMessage;
import org.betonquest.betonquest.menu.MenuID;
import org.betonquest.betonquest.menu.RPGMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * The RPG menu main command.
 */
public class RPGMenuCommand extends SimpleCommand {
    /**
     * RPGMenu instance this command works on.
     */
    private final RPGMenu menu;

    /**
     * Create a new RPGMenu command.
     *
     * @param log  the custom logger instance for this class
     * @param menu the rpg menu this command works on
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public RPGMenuCommand(final BetonQuestLogger log, final RPGMenu menu) {
        super(log, "rpgmenu", new Permission("betonquest.admin"), 0, "qm", "menu", "menus", "rpgmenus", "rpgm");
        this.menu = menu;
        setDescription("Core command of the RPGMenu addon for BetonQuest");
        setUsage("/rpgmenu <reload/open/list>");
        register();
    }

    @Override
    @SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.AvoidLiteralsInIfCondition", "PMD.CyclomaticComplexity"})
    public List<String> simpleTabComplete(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "open", "list");
        }
        if (args.length > 2) {
            return new ArrayList<>();
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            //complete menu ids
            case "open":
            case "o":
            case "reload":
                if (!args[1].contains(".")) {
                    return new ArrayList<>(BetonQuest.getInstance().getPackages().keySet());
                }
                final String pack = args[1].substring(0, args[1].indexOf('.'));
                final QuestPackage configPack = BetonQuest.getInstance().getPackages().get(pack);
                if (configPack == null) {
                    return new ArrayList<>();
                }
                final List<String> completions = new ArrayList<>();
                for (final MenuID id : menu.getMenus()) {
                    if (id.getPackage().equals(configPack)) {
                        completions.add(id.toString());
                    }
                }
                return completions;
            default:
                return new ArrayList<>();
        }
    }

    @Override
    @SuppressWarnings({"PMD.NcssCount", "PMD.CognitiveComplexity", "PMD.CyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.AvoidLiteralsInIfCondition", "PMD.SwitchDensity", "PMD.NonExhaustiveSwitch"})
    public boolean simpleCommand(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 0) {
            //display command help
            showHelp(sender);
            return false;
        }
        MenuID menu = null;
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "l":
            case "list":
                break;
            case "o":
            case "open":
            case "reload":
                //parse menu id
                if (args.length >= 2) {
                    try {
                        menu = new MenuID(null, args[1]);
                    } catch (final QuestException e) {
                        sendMessage(sender, "command_invalid_menu", new PluginMessage.Replacement("menu", Component.text(args[1])));
                        return false;
                    }
                }
                break;
            default:
                // diplay help
                showHelp(sender);
                return false;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "l":
            case "list":
                final TextComponent.Builder builder = Component.text();
                builder.append(getMessage(sender, "command_list"));
                final Collection<MenuID> ids = this.menu.getMenus();
                if (ids.isEmpty()) {
                    builder.append(Component.newline().append(Component.text(" - ").color(NamedTextColor.GRAY)));
                } else {
                    for (final MenuID menuID : ids) {
                        builder
                                .append(Component.newline().append(Component.text(menuID.toString()).color(NamedTextColor.GRAY))
                                        .hoverEvent(getMessage(sender, "click_to_open"))
                                        .clickEvent(ClickEvent.runCommand("/" + getName() + " open " + menuID)));
                    }
                }
                sender.sendMessage(builder.asComponent());
                return true;
            case "o":
            case "open":
                Player player = null;
                //parse player
                if (args.length >= 3) {
                    player = Bukkit.getPlayer(args[2]);
                    if (player == null) {
                        sendMessage(sender, "command_invalid_player", new PluginMessage.Replacement("player", Component.text(args[1])));
                        return false;
                    }
                }
                //handle unspecified players
                if (player == null) {
                    if (sender instanceof Player) {
                        player = (Player) sender;
                    } else {
                        sendMessage(sender, "command_no_player");
                        return false;
                    }
                }
                //handle unspecified ids
                if (menu == null) {
                    sendMessage(sender, "command_no_menu");
                    return false;
                }
                //open the menu and send feedback
                this.menu.openMenu(BetonQuest.getInstance().getProfileProvider().getProfile(player), menu);
                sendMessage(sender, "command_open_successful", new PluginMessage.Replacement("menu", Component.text(menu.toString())));
                break;
            case "reload":
                final RPGMenu.ReloadInformation info;
                ChatColor color = ChatColor.GRAY;
                if (menu == null) {
                    //reload all data
                    info = this.menu.reloadData();
                } else {
                    // reload one menu
                    info = this.menu.reloadMenu(menu);
                }
                //notify player, console gets automatically informed
                if (sender instanceof Player) {
                    switch (info.getResult()) {
                        case FAILED:
                            for (final String errorMessage : info.getErrorMessages()) {
                                sender.sendMessage(errorMessage);
                            }
                            sendMessage(sender, "command_reload_failed");
                            return true;
                        case SUCCESS:
                            color = ChatColor.YELLOW;
                            break;
                        case FULL_SUCCESS:
                            color = ChatColor.GREEN;
                            break;
                    }
                    for (final String errorMessage : info.getErrorMessages()) {
                        sender.sendMessage(errorMessage);
                    }
                    sendMessage(sender, "command_reload_successful",
                            new PluginMessage.Replacement("color", Component.text(color.toString())),
                            new PluginMessage.Replacement("amount", Component.text(info.getLoaded())));
                }
        }
        return true;
    }

    /**
     * Displays the full help message to the command sender.
     *
     * @param sender player who issued the command or console
     */
    private void showHelp(final CommandSender sender) {
        final TextComponent.Builder builder = Component.text();
        builder
                .append(Component.text("----- RPGMenu for Betonquest -----").append(Component.newline()))
                .append(Component.text("/rpgmenu reload [menu]").color(NamedTextColor.RED)
                        .hoverEvent(getMessage(sender, "command_info_reload"))
                        .clickEvent(ClickEvent.suggestCommand("/rpgmenu reload "))
                        .append(Component.newline()))
                .append(Component.text("/rpgmenu open <menu> [player]").color(NamedTextColor.RED)
                        .hoverEvent(getMessage(sender, "command_info_open"))
                        .clickEvent(ClickEvent.suggestCommand("/rpgmenu open"))
                        .append(Component.newline()))
                .append(Component.text("/rpgmenu list").color(NamedTextColor.RED)
                        .hoverEvent(getMessage(sender, "command_info_list"))
                        .clickEvent(ClickEvent.suggestCommand("/rpgmenu list")));
        sender.sendMessage(builder.asComponent());
    }
}
