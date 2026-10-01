package org.betonquest.betonquest.feature.questlog;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Admin command for the quest log: {@code /questlog open|clear-trackers <player>},
 * {@code /questlog add|set <player> <quest> <stage>} and {@code /questlog complete|del <player> <quest>}.
 * <p>
 * Also registered as the legacy {@code /sqj} command, which takes quests as {@code Package.quest}.
 * TODO(remove after package rewrite): drop the sqj command once no quest package or plugin calls it.
 */
public class QuestLogCommand implements TabExecutor {

    /**
     * The subcommands.
     */
    private static final List<String> SUBCOMMANDS = List.of("open", "clear-trackers", "add", "set", "complete", "del");

    /**
     * The quest log to change.
     */
    private final QuestLog questLog;

    /**
     * The profile provider to get profiles of players.
     */
    private final ProfileProvider profileProvider;

    /**
     * Create a new quest log command.
     *
     * @param questLog        the quest log to change
     * @param profileProvider the profile provider to get profiles of players
     */
    public QuestLogCommand(final QuestLog questLog, final ProfileProvider profileProvider) {
        this.questLog = questLog;
        this.profileProvider = profileProvider;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String subcommand = arg(args, 0).toLowerCase(Locale.ROOT);
        final Player player = Bukkit.getPlayerExact(arg(args, 1));
        if (!SUBCOMMANDS.contains(subcommand) || player == null) {
            return false;
        }
        final OnlineProfile profile = profileProvider.getProfile(player);
        final String quest = arg(args, 2);
        final String stage = arg(args, 3);
        try {
            switch (subcommand) {
                case "open" -> {
                    if (!questLog.open(player)) {
                        sender.sendMessage("There is no quest log menu, is MenuAPI installed?");
                    }
                }
                case "clear-trackers" -> questLog.untrackAll(profile);
                default -> changeQuest(sender, profile, subcommand,
                        "sqj".equalsIgnoreCase(command.getName()) ? quest.replaceFirst("\\.", ">") : quest, stage);
            }
        } catch (final QuestException e) {
            sender.sendMessage(e.getMessage());
        }
        return true;
    }

    private String arg(final String[] args, final int index) {
        return index < args.length ? args[index] : "";
    }

    private void changeQuest(final CommandSender sender, final OnlineProfile profile, final String subcommand,
                             final String quest, final String stage) throws QuestException {
        final QuestLogQuest definition = questLog.getQuests().get(quest);
        if (definition == null) {
            sender.sendMessage("Unknown quest '" + quest + "'");
            return;
        }
        final boolean needsStage = "add".equals(subcommand) || "set".equals(subcommand);
        if (needsStage && !definition.stages().containsKey(stage)) {
            sender.sendMessage("Unknown stage '" + stage + "' of quest " + quest);
            return;
        }
        switch (subcommand) {
            case "add" -> questLog.add(profile, quest, stage);
            case "set" -> questLog.set(profile, quest, stage);
            case "complete" -> questLog.complete(profile, quest);
            default -> questLog.delete(profile, quest);
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final String last = args[args.length - 1].toLowerCase(Locale.ROOT);
        final List<String> options = switch (args.length) {
            case 1 -> SUBCOMMANDS;
            case 2 -> Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();
            default -> List.of();
        };
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(last)).toList();
    }
}
