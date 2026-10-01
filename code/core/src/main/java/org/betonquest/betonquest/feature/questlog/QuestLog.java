package org.betonquest.betonquest.feature.questlog;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;
import org.betonquest.betonquest.data.PlayerDataStorage;
import org.betonquest.betonquest.notify.io.AdvancementNotifyIO;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Changes the quest log of players and shows their tracked quests on the sidebar.
 * <p>
 * Menus and waypoints come from integrations, see {@link #setMenu} and {@link #setWaypoints}.
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.TooManyMethods", "PMD.GodClass"})
public class QuestLog implements Listener {

    /**
     * How many quests a player can track at once.
     */
    public static final int MAX_TRACKED = 2;

    /**
     * Colors of the tracked quests, by tracking order.
     */
    public static final List<String> COLORS = List.of("<light_purple>", "<aqua>");

    /**
     * Icons of the tracked quests, by tracking order.
     */
    public static final List<String> ICONS = List.of("①", "②");

    /**
     * Name of the sidebar objective.
     */
    private static final String OBJECTIVE = "questlog";

    /**
     * Prefix of the sidebar line teams.
     */
    private static final String TEAM_PREFIX = "questlog_";

    /**
     * Characters per sidebar line.
     */
    private static final int WRAP_WIDTH = 30;

    /**
     * Lines the sidebar can show.
     */
    private static final int MAX_LINES = 15;

    /**
     * Ticks to wait before trying again when another plugin uses the sidebar.
     */
    private static final long SIDEBAR_RETRY = 20 * 30;

    /**
     * Parser for the quest log texts.
     */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * The plugin instance used for scheduling.
     */
    private final Plugin plugin;

    /**
     * The loaded quests.
     */
    private final QuestLogProcessor quests;

    /**
     * The storage of the quest log entries.
     */
    private final PlayerDataStorage playerDataStorage;

    /**
     * The profile provider to get profiles of joining players.
     */
    private final ProfileProvider profileProvider;

    /**
     * The placeholder manager for the completion toast.
     */
    private final PlaceholderManager placeholders;

    /**
     * The waypoints of quest stages.
     */
    private Waypoints waypoints = Waypoints.NONE;

    /**
     * Opens the quest log menu, or null if there is none.
     */
    @Nullable
    private Consumer<Player> menu;

    /**
     * Formats the scores of sidebar lines, used to hide the numbers on servers that support it.
     */
    private Consumer<Score> scoreFormat = score -> {
    };

    /**
     * Create a new quest log.
     *
     * @param plugin            the plugin instance used for scheduling
     * @param quests            the loaded quests
     * @param playerDataStorage the storage of the quest log entries
     * @param profileProvider   the profile provider to get profiles of joining players
     * @param placeholders      the placeholder manager for the completion toast
     */
    public QuestLog(final Plugin plugin, final QuestLogProcessor quests, final PlayerDataStorage playerDataStorage,
                    final ProfileProvider profileProvider, final PlaceholderManager placeholders) {
        this.plugin = plugin;
        this.quests = quests;
        this.playerDataStorage = playerDataStorage;
        this.profileProvider = profileProvider;
        this.placeholders = placeholders;
    }

    /**
     * Sets the waypoints of quest stages.
     *
     * @param waypoints the waypoints
     */
    public void setWaypoints(final Waypoints waypoints) {
        this.waypoints = waypoints;
    }

    /**
     * Sets how the scores of sidebar lines are formatted.
     *
     * @param scoreFormat formats a score, e.g. to hide its number
     */
    public void setScoreFormat(final Consumer<Score> scoreFormat) {
        this.scoreFormat = scoreFormat;
    }

    /**
     * Sets how the quest log menu is opened.
     *
     * @param menu opens the menu for a player
     */
    public void setMenu(final Consumer<Player> menu) {
        this.menu = menu;
    }

    /**
     * Opens the quest log menu.
     *
     * @param player the player to open it for
     * @return false if there is no menu
     */
    public boolean open(final Player player) {
        if (menu == null) {
            return false;
        }
        menu.accept(player);
        return true;
    }

    /**
     * Get the loaded quests.
     *
     * @return the quest processor
     */
    public QuestLogProcessor getQuests() {
        return quests;
    }

    /**
     * Get the quest log entries of a profile.
     *
     * @param profile the profile
     * @return the entries by full quest identifier
     */
    public Map<String, QuestLogEntry> getEntries(final Profile profile) {
        return playerDataStorage.get(profile).getQuestLog();
    }

    /**
     * Starts a quest at a stage and tracks it, replacing the oldest tracked quest if needed.
     *
     * @param profile the profile
     * @param quest   the full quest identifier
     * @param stage   the stage to start at
     */
    public void add(final OnlineProfile profile, final String quest, final String stage) {
        final List<String> tracked = getTracked(profile);
        if (!tracked.contains(quest) && tracked.size() >= MAX_TRACKED) {
            untrack(profile, tracked.get(0));
        }
        playerDataStorage.get(profile).setQuestLogEntry(quest, new QuestLogEntry(stage, false, System.currentTimeMillis()));
        final Player player = profile.getPlayer();
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 3.0f, 1.0f);
        compass(quest, stage, compass -> waypoints.add(player, compass));
        updateSidebar(profile);
        trackWaypoint(profile, quest);
    }

    /**
     * Moves a started quest to another stage.
     *
     * @param profile the profile
     * @param quest   the full quest identifier
     * @param stage   the new stage
     * @throws QuestException if the quest was not started
     */
    public void set(final OnlineProfile profile, final String quest, final String stage) throws QuestException {
        final QuestLogEntry old = require(profile, quest);
        playerDataStorage.get(profile).setQuestLogEntry(quest, new QuestLogEntry(stage, false, old.tracked()));
        final Player player = profile.getPlayer();
        player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 3.0f, 1.5f);
        player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 10.0f, 1.5f);
        compass(quest, old.stage(), compass -> waypoints.remove(player, compass));
        compass(quest, stage, compass -> waypoints.add(player, compass));
        if (old.isTracked()) {
            updateSidebar(profile);
            trackWaypoint(profile, quest);
        }
    }

    /**
     * Completes a started quest and shows a toast.
     *
     * @param profile the profile
     * @param quest   the full quest identifier
     * @throws QuestException if the quest was not started
     */
    public void complete(final OnlineProfile profile, final String quest) throws QuestException {
        final QuestLogEntry old = require(profile, quest);
        playerDataStorage.get(profile).setQuestLogEntry(quest, new QuestLogEntry(old.stage(), true, 0));
        final Player player = profile.getPlayer();
        player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        compass(quest, old.stage(), compass -> waypoints.remove(player, compass));
        updateSidebar(profile);
        final QuestLogQuest definition = quests.get(quest);
        if (definition != null) {
            new AdvancementNotifyIO(placeholders, null, Map.of("frame", "task", "icon", "minecraft:writable_book"), plugin)
                    .sendNotify(MINI_MESSAGE.deserialize(definition.name()), profile);
        }
    }

    /**
     * Removes a quest from the quest log.
     *
     * @param profile the profile
     * @param quest   the full quest identifier
     */
    public void delete(final OnlineProfile profile, final String quest) {
        final QuestLogEntry old = getEntries(profile).get(quest);
        if (old == null) {
            return;
        }
        playerDataStorage.get(profile).removeQuestLogEntry(quest);
        compass(quest, old.stage(), compass -> waypoints.remove(profile.getPlayer(), compass));
        if (old.isTracked()) {
            updateSidebar(profile);
        }
    }

    /**
     * Get the tracked quests in tracking order.
     *
     * @param profile the profile
     * @return the full identifiers of the tracked quests
     */
    public List<String> getTracked(final Profile profile) {
        return getEntries(profile).entrySet().stream()
                .filter(entry -> entry.getValue().isTracked() && !entry.getValue().complete())
                .sorted(Comparator.comparingLong(entry -> entry.getValue().tracked()))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Tracks a started quest on the sidebar.
     *
     * @param profile the profile
     * @param quest   the full quest identifier
     * @return false if the player already tracks the maximum amount of quests
     */
    public boolean track(final OnlineProfile profile, final String quest) {
        final QuestLogEntry entry = getEntries(profile).get(quest);
        if (entry == null || entry.isTracked()) {
            return entry != null;
        }
        if (getTracked(profile).size() >= MAX_TRACKED) {
            return false;
        }
        playerDataStorage.get(profile).setQuestLogEntry(quest, new QuestLogEntry(entry.stage(), entry.complete(), System.currentTimeMillis()));
        updateSidebar(profile);
        trackWaypoint(profile, quest);
        return true;
    }

    /**
     * Removes a quest from the sidebar.
     *
     * @param profile the profile
     * @param quest   the full quest identifier
     */
    public void untrack(final OnlineProfile profile, final String quest) {
        final QuestLogEntry entry = getEntries(profile).get(quest);
        if (entry == null || !entry.isTracked()) {
            return;
        }
        playerDataStorage.get(profile).setQuestLogEntry(quest, new QuestLogEntry(entry.stage(), entry.complete(), 0));
        updateSidebar(profile);
        compass(quest, entry.stage(), compass -> waypoints.untrack(profile.getPlayer(), compass));
    }

    /**
     * Removes all quests from the sidebar.
     *
     * @param profile the profile
     */
    public void untrackAll(final OnlineProfile profile) {
        getTracked(profile).forEach(quest -> untrack(profile, quest));
    }

    /**
     * Shows the tracked quests on the sidebar and restores their waypoints.
     *
     * @param event the join event
     */
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final OnlineProfile profile = profileProvider.getProfile(event.getPlayer());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!profile.getPlayer().isOnline()) {
                return;
            }
            updateSidebar(profile);
            getTracked(profile).forEach(quest -> trackWaypoint(profile, quest));
        }, 5);
    }

    private QuestLogEntry require(final Profile profile, final String quest) throws QuestException {
        final QuestLogEntry entry = getEntries(profile).get(quest);
        if (entry == null) {
            throw new QuestException("Quest '" + quest + "' was not added to the quest log of " + profile);
        }
        return entry;
    }

    private void compass(final String quest, final String stage, final Consumer<String> action) {
        final QuestLogQuest definition = quests.get(quest);
        final QuestLogQuest.Stage stageDefinition = definition == null ? null : definition.stages().get(stage);
        if (stageDefinition != null && stageDefinition.compass() != null) {
            action.accept(stageDefinition.compass());
        }
    }

    private void trackWaypoint(final OnlineProfile profile, final String quest) {
        final int index = getTracked(profile).indexOf(quest);
        final QuestLogEntry entry = getEntries(profile).get(quest);
        if (index < 0 || entry == null) {
            return;
        }
        compass(quest, entry.stage(), compass -> waypoints.track(profile.getPlayer(), compass, COLORS.get(index), ICONS.get(index)));
    }

    /**
     * Shows the tracked quests on the sidebar, or removes the sidebar if none are tracked.
     *
     * @param profile the profile
     */
    public void updateSidebar(final OnlineProfile profile) {
        final Scoreboard scoreboard = profile.getPlayer().getScoreboard();
        final Objective current = scoreboard.getObjective(OBJECTIVE);
        final List<String> lines = sidebarLines(profile);
        if (lines.isEmpty()) {
            if (current != null) {
                current.unregister();
            }
            removeTeams(scoreboard);
            return;
        }
        final Objective shown = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (shown != null && !shown.equals(current)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (profile.getPlayer().isOnline()) {
                    updateSidebar(profile);
                }
            }, SIDEBAR_RETRY);
            return;
        }
        if (current != null) {
            current.unregister();
        }
        removeTeams(scoreboard);
        final Objective objective = scoreboard.registerNewObjective(OBJECTIVE, "dummy", MINI_MESSAGE.deserialize("<gold><bold>Quests"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        final int count = Math.min(lines.size(), MAX_LINES);
        for (int i = 0; i < count; i++) {
            final String entry = "§" + Integer.toHexString(i);
            final Team team = scoreboard.registerNewTeam(TEAM_PREFIX + i);
            team.addEntry(entry);
            team.prefix(MINI_MESSAGE.deserialize(lines.get(i)));
            final Score score = objective.getScore(entry);
            scoreFormat.accept(score);
            score.setScore(count - i);
        }
    }

    private List<String> sidebarLines(final Profile profile) {
        final List<String> lines = new ArrayList<>();
        final List<String> tracked = getTracked(profile);
        for (int i = 0; i < tracked.size(); i++) {
            final QuestLogQuest quest = quests.get(tracked.get(i));
            final QuestLogEntry entry = getEntries(profile).get(tracked.get(i));
            final QuestLogQuest.Stage stage = quest == null || entry == null ? null : quest.stages().get(entry.stage());
            if (stage == null) {
                continue;
            }
            final List<String> name = wrap(stage.name() + ":");
            lines.add(COLORS.get(i) + ICONS.get(i) + " <yellow>" + name.get(0));
            name.subList(1, name.size()).forEach(line -> lines.add("  <yellow>" + line));
            wrap(stage.objective()).forEach(line -> lines.add("   <gray>" + line));
        }
        return lines;
    }

    private void removeTeams(final Scoreboard scoreboard) {
        for (final Team team : List.copyOf(scoreboard.getTeams())) {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                team.unregister();
            }
        }
    }

    /**
     * Wraps text at spaces into lines of at most {@link #WRAP_WIDTH} characters; longer words get their own line.
     *
     * @param text the text to wrap
     * @return the lines
     */
    public static List<String> wrap(final String text) {
        final List<String> lines = new ArrayList<>();
        final StringBuilder line = new StringBuilder();
        for (final String word : text.split(" ")) {
            if (!line.isEmpty() && line.length() + 1 + word.length() > WRAP_WIDTH) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty()) {
                line.append(' ');
            }
            line.append(word);
        }
        lines.add(line.toString());
        return lines;
    }

    /**
     * Shows quest stage locations, e.g. on a map. The compass is the waypoint name from the stage.
     */
    public interface Waypoints {

        /**
         * Does nothing, used when no waypoint integration is present.
         */
        Waypoints NONE = new Waypoints() {
            @Override
            public void add(final Player player, final String compass) {
                // Empty
            }

            @Override
            public void remove(final Player player, final String compass) {
                // Empty
            }

            @Override
            public void track(final Player player, final String compass, final String color, final String icon) {
                // Empty
            }

            @Override
            public void untrack(final Player player, final String compass) {
                // Empty
            }
        };

        /**
         * Adds a waypoint for the player.
         *
         * @param player  the player
         * @param compass the waypoint
         */
        void add(Player player, String compass);

        /**
         * Removes a waypoint for the player.
         *
         * @param player  the player
         * @param compass the waypoint
         */
        void remove(Player player, String compass);

        /**
         * Highlights a waypoint as tracked.
         *
         * @param player  the player
         * @param compass the waypoint
         * @param color   the MiniMessage color tag of the tracked quest
         * @param icon    the icon of the tracked quest
         */
        void track(Player player, String compass, String color, String icon);

        /**
         * Removes the tracked highlight of a waypoint.
         *
         * @param player  the player
         * @param compass the waypoint
         */
        void untrack(Player player, String compass);
    }
}
