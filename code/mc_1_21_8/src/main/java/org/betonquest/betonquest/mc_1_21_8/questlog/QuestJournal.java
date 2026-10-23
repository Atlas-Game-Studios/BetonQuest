package org.betonquest.betonquest.mc_1_21_8.questlog;

import com.ags.menuapi.dialog.Dialogs;
import com.ags.menuapi.dialog.Drawn;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.feature.questlog.QuestLog;
import org.betonquest.betonquest.feature.questlog.QuestLogEntry;
import org.betonquest.betonquest.feature.questlog.QuestLogQuest;
import org.betonquest.betonquest.id.questlog.QuestLogIdentifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The quest journal: one dialog with a filter line (active, main, side, dailies, completed) instead of a tree of menus.
 * Quest lines are clickable text: a click tracks or untracks an active quest, the hover shows the details.
 * Where a quest is listed comes from {@code questlog.categories} in the config: category id to region and type.
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.TooManyMethods", "PMD.GodClass"})
public class QuestJournal {

    /**
     * Parser for the journal texts.
     */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Width of a dialog body block; lines are kept shorter than this so they never wrap.
     */
    private static final int BLOCK_WIDTH = 320;

    /**
     * Every block is padded to this many pixels so all blocks share one left edge.
     */
    private static final int TEXT_WIDTH = 290;

    /**
     * Characters per objective line.
     */
    private static final int WRAP = 46;

    /**
     * Width of the footer buttons.
     */
    private static final int BUTTON_WIDTH = 150;

    /**
     * Clickable text in the journal works once, for as long as the journal is likely open.
     */
    private static final ClickCallback.Options ONCE = ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(10)).build();

    /**
     * Region and type of quests whose category is not in the config.
     */
    private static final Category UNKNOWN = new Category("Other", Type.SIDE);

    /**
     * Custom logger for config problems.
     */
    private final BetonQuestLogger log;

    /**
     * The config to read the categories from.
     */
    private final ConfigurationSection config;

    /**
     * The quest log to show.
     */
    private final QuestLog questLog;

    /**
     * The profile provider to get profiles of players.
     */
    private final ProfileProvider profileProvider;

    /**
     * Problems already logged, so each is logged once.
     */
    private final Set<String> reported = new HashSet<>();

    /**
     * Create the quest journal.
     *
     * @param log             the custom logger for config problems
     * @param config          the plugin config to read the categories from
     * @param questLog        the quest log to show
     * @param profileProvider the profile provider to get profiles of players
     */
    public QuestJournal(final BetonQuestLogger log, final ConfigurationSection config, final QuestLog questLog,
                        final ProfileProvider profileProvider) {
        this.log = log;
        this.config = config;
        this.questLog = questLog;
        this.profileProvider = profileProvider;
    }

    /**
     * Opens the journal on the active quests.
     *
     * @param player the player
     */
    public void open(final Player player) {
        open(player, Filter.ACTIVE, null);
    }

    private void open(final Player player, final Filter filter, @Nullable final String expanded) {
        final OnlineProfile profile = profileProvider.getProfile(player);
        final Map<String, QuestLogEntry> entries = questLog.getEntries(profile);
        final List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(filterLine(filter), BLOCK_WIDTH));
        switch (filter) {
            case ACTIVE -> active(player, profile, entries, body);
            case MAIN -> story(player, profile, entries, body, filter, Set.of(Type.MAIN, Type.TUTORIAL));
            case SIDE -> story(player, profile, entries, body, filter, Set.of(Type.SIDE));
            case DAILIES -> dailies(player, profile, entries, body);
            default -> completed(entries, body, expanded);
        }
        Dialogs.notice(player, Drawn.title("<white>Quest Journal"), body,
                Dialogs.button("<white>Backpack", null, BUTTON_WIDTH, clicker -> clicker.performCommand("backpack")),
                Dialogs.button("Close", null, BUTTON_WIDTH, clicker -> {
                }));
    }

    private Component filterLine(final Filter current) {
        final List<Component> parts = new ArrayList<>();
        for (final Filter filter : Filter.values()) {
            if (!parts.isEmpty()) {
                parts.add(Component.text("   "));
            }
            final String label = filter == current ? "<white><u>" + filter.label + "</u>" : "<gray>" + filter.label;
            parts.add(MINI_MESSAGE.deserialize(label)
                    .hoverEvent(HoverEvent.showText(MINI_MESSAGE.deserialize("<gray>Show " + filter.hint)))
                    .clickEvent(ClickEvent.callback(audience -> {
                        if (audience instanceof final Player player) {
                            open(player, filter, null);
                        }
                    }, ONCE)));
        }
        return Component.textOfChildren(parts.toArray(new Component[0]));
    }

    // ------------------------------------------------------------------ views

    private void active(final Player player, final OnlineProfile profile, final Map<String, QuestLogEntry> entries,
                        final List<DialogBody> body) {
        final List<String> tracked = questLog.getTracked(profile);
        if (!tracked.isEmpty()) {
            final Block block = new Block();
            block.heading("<gray>TRACKED");
            for (final String quest : tracked) {
                activeQuest(player, profile, entries, quest, Filter.ACTIVE, block);
            }
            block.addTo(body);
        }
        final Map<String, List<String>> byRegion = new LinkedHashMap<>();
        for (final Map.Entry<String, QuestLogEntry> entry : new TreeMap<>(entries).entrySet()) {
            final QuestLogQuest quest = questLog.getQuests().get(entry.getKey());
            if (quest == null || entry.getValue().complete() || tracked.contains(entry.getKey())
                    || category(quest).type == Type.DAILY) {
                continue;
            }
            byRegion.computeIfAbsent(category(quest).region, region -> new ArrayList<>()).add(entry.getKey());
        }
        if (tracked.isEmpty() && byRegion.isEmpty()) {
            body.add(DialogBody.plainMessage(MINI_MESSAGE.deserialize("<gray>No active quests. People with something to ask are marked on your map."), BLOCK_WIDTH));
            return;
        }
        for (final String region : regionOrder(byRegion.keySet())) {
            final Block block = new Block();
            block.heading("<gray>" + region.toUpperCase(Locale.ROOT));
            for (final String quest : byRegion.getOrDefault(region, List.of())) {
                activeQuest(player, profile, entries, quest, Filter.ACTIVE, block);
            }
            block.addTo(body);
        }
    }

    private void dailies(final Player player, final OnlineProfile profile, final Map<String, QuestLogEntry> entries,
                         final List<DialogBody> body) {
        final Block block = new Block();
        for (final Map.Entry<String, QuestLogEntry> entry : new TreeMap<>(entries).entrySet()) {
            final QuestLogQuest quest = questLog.getQuests().get(entry.getKey());
            if (quest != null && !entry.getValue().complete() && category(quest).type == Type.DAILY) {
                activeQuest(player, profile, entries, entry.getKey(), Filter.DAILIES, block);
            }
        }
        if (block.isEmpty()) {
            body.add(DialogBody.plainMessage(MINI_MESSAGE.deserialize("<gray>No dailies right now. New ones come round each day."), BLOCK_WIDTH));
            return;
        }
        block.addTo(body);
    }

    /**
     * Every quest of the given types by region, in quest-line order. Locked quests are only counted, not named.
     */
    private void story(final Player player, final OnlineProfile profile, final Map<String, QuestLogEntry> entries,
                       final List<DialogBody> body, final Filter filter, final Set<Type> types) {
        final Map<String, Map<String, QuestLogQuest>> byRegion = new LinkedHashMap<>();
        for (final Map.Entry<QuestLogIdentifier, QuestLogQuest> entry : questLog.getQuests().getValues().entrySet()) {
            final Category category = category(entry.getValue());
            if (types.contains(category.type)) {
                byRegion.computeIfAbsent(category.region, region -> new TreeMap<>()).put(entry.getKey().getFull(), entry.getValue());
            }
        }
        for (final String region : regionOrder(byRegion.keySet())) {
            final Block block = new Block();
            int done = 0;
            int locked = 0;
            final List<Map.Entry<String, QuestLogQuest>> quests = questLines(byRegion.getOrDefault(region, Map.of()));
            for (final Map.Entry<String, QuestLogQuest> quest : quests) {
                final QuestLogEntry entry = entries.get(quest.getKey());
                if (entry != null && entry.complete()) {
                    done++;
                }
                if (entry == null && isLocked(quest.getValue(), entries)) {
                    locked++;
                }
            }
            block.heading("<gray>" + region.toUpperCase(Locale.ROOT) + "  <dark_gray>" + done + "/" + quests.size() + " done");
            for (final Map.Entry<String, QuestLogQuest> quest : quests) {
                final QuestLogEntry entry = entries.get(quest.getKey());
                if (entry == null && isLocked(quest.getValue(), entries)) {
                    continue;
                }
                if (entry != null && !entry.complete()) {
                    activeQuest(player, profile, entries, quest.getKey(), filter, block);
                } else if (entry != null) {
                    block.add("<dark_gray>" + quest.getValue().name() + "  <dark_green>done", hover(details(quest.getValue(), null, null)));
                } else {
                    final Style style = hover(details(quest.getValue(), null, "<gray>Not started yet."));
                    final String name = "<white>" + quest.getValue().name();
                    final String start = quest.getValue().npc() == null ? "" : "<dark_gray>talk to " + quest.getValue().npc();
                    if (Drawn.width(name + "  " + start) <= TEXT_WIDTH) {
                        block.add(name + "  " + start, style);
                    } else {
                        // too wide for one line: a wrapped block would lose its left edge
                        block.add(name, style);
                        block.add("<dark_gray>    " + start, style);
                    }
                }
            }
            if (locked > 0) {
                block.add("<dark_gray>" + locked + (locked == 1 ? " quest opens" : " quests open") + " up later", Style.empty());
            }
            block.addTo(body);
        }
    }

    private void completed(final Map<String, QuestLogEntry> entries, final List<DialogBody> body, @Nullable final String expanded) {
        final Map<String, int[]> counts = new LinkedHashMap<>();
        final Map<String, List<String>> names = new LinkedHashMap<>();
        for (final Map.Entry<QuestLogIdentifier, QuestLogQuest> entry : questLog.getQuests().getValues().entrySet()) {
            final Category category = category(entry.getValue());
            if (category.type == Type.DAILY) {
                continue;
            }
            final int[] count = counts.computeIfAbsent(category.region, region -> new int[2]);
            count[1]++;
            final QuestLogEntry progress = entries.get(entry.getKey().getFull());
            if (progress != null && progress.complete()) {
                count[0]++;
                names.computeIfAbsent(category.region, region -> new ArrayList<>()).add(entry.getValue().name());
            }
        }
        final Block block = new Block();
        for (final String region : regionOrder(counts.keySet())) {
            final int[] count = counts.getOrDefault(region, new int[2]);
            final boolean open = region.equals(expanded);
            final String toggle = count[0] == 0 ? "" : open ? "  <dark_gray>hide" : "  <dark_gray>show";
            block.add("<gray>" + region + "  <white>" + count[0] + "<gray>/" + count[1] + toggle, count[0] == 0 ? Style.empty()
                    : Style.style().clickEvent(ClickEvent.callback(audience -> {
                        if (audience instanceof final Player player) {
                            open(player, Filter.COMPLETED, open ? null : region);
                        }
                    }, ONCE)).build());
            if (open) {
                names.getOrDefault(region, List.of()).stream().sorted().forEach(name -> block.add("<dark_gray>   " + name, Style.empty()));
            }
        }
        block.addTo(body);
    }

    // ------------------------------------------------------------------ one quest

    /**
     * An active quest: its name and stage, then the objective. Clicking tracks or untracks it.
     */
    private void activeQuest(final Player player, final OnlineProfile profile, final Map<String, QuestLogEntry> entries,
                             final String questId, final Filter filter, final Block block) {
        final QuestLogQuest quest = questLog.getQuests().get(questId);
        final QuestLogEntry entry = entries.get(questId);
        if (quest == null || entry == null) {
            return;
        }
        final List<String> tracked = questLog.getTracked(profile);
        final int index = tracked.indexOf(questId);
        final List<String> stageIds = new ArrayList<>(quest.stages().keySet());
        final int stage = stageIds.indexOf(entry.stage()) + 1;
        final String marker = index >= 0 ? QuestLog.COLORS.get(index) + "[" + (index + 1) + "] " : "<gold>- ";
        final String progress = quest.stages().size() > 1 && stage > 0 ? "  <gray>" + stage + "/" + quest.stages().size() : "";
        final String action;
        if (index >= 0) {
            action = "<yellow>Click to stop tracking.";
        } else if (tracked.size() < QuestLog.MAX_TRACKED) {
            action = "<yellow>Click to track on your sidebar and map.";
        } else {
            action = "<red>You track " + QuestLog.MAX_TRACKED + " quests already; click one of them to stop first.";
        }
        final Style style = hover(details(quest, stage > 0 ? "<gray>Stage " + stage + " of " + quest.stages().size() : null, action))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (index >= 0) {
                        questLog.untrack(profile, questId);
                    } else {
                        questLog.track(profile, questId);
                    }
                    open(player, filter, null);
                }, ONCE));
        block.add(marker + "<white>" + quest.name() + progress, style);
        final QuestLogQuest.Stage current = quest.stages().get(entry.stage());
        if (current != null) {
            for (final String line : wrap(current.objective())) {
                block.add("<gray>    " + line, style);
            }
        }
    }

    private String details(final QuestLogQuest quest, @Nullable final String stage, @Nullable final String action) {
        final Category category = category(quest);
        final StringBuilder text = new StringBuilder("<white>").append(quest.name())
                .append("<newline><gray>").append(category.region).append(", ").append(category.type.label);
        if (stage != null) {
            text.append("<newline>").append(stage);
        }
        if (quest.npc() != null) {
            text.append("<newline><gray>Starts with <white>").append(quest.npc());
        }
        if (!quest.prereqs().isEmpty()) {
            text.append("<newline><gray>Follows <white>").append(String.join("<gray>, <white>", quest.prereqs().stream()
                    .map(id -> {
                        final QuestLogQuest prereq = questLog.getQuests().get(id);
                        return prereq == null ? id.substring(id.indexOf('>') + 1) : prereq.name();
                    }).toList()));
        }
        if (action != null) {
            text.append("<newline>").append(action);
        }
        return text.toString();
    }

    private Style hover(final String text) {
        return Style.style().hoverEvent(HoverEvent.showText(MINI_MESSAGE.deserialize(text))).build();
    }

    /**
     * A quest is locked while any quest it follows is not done. A prerequisite that names no quest locks nothing.
     */
    private boolean isLocked(final QuestLogQuest quest, final Map<String, QuestLogEntry> entries) {
        for (final String id : quest.prereqs()) {
            final QuestLogEntry prereq = entries.get(id);
            if (questLog.getQuests().get(id) != null && (prereq == null || !prereq.complete())) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ categories and order

    private Category category(final QuestLogQuest quest) {
        final ConfigurationSection section = config.getConfigurationSection("questlog.categories." + quest.category());
        if (section == null) {
            report("Quest log category '" + quest.category() + "' (quest '" + quest.name()
                    + "') is not in questlog.categories; it is listed under Other.");
            return UNKNOWN;
        }
        final String typeName = section.getString("type", "side").toUpperCase(Locale.ROOT);
        Type type = Type.SIDE;
        for (final Type candidate : Type.values()) {
            if (candidate.name().equals(typeName)) {
                type = candidate;
            }
        }
        return new Category(section.getString("region", UNKNOWN.region), type);
    }

    /**
     * Regions in the order the config first names them; regions it doesn't name come last.
     */
    private List<String> regionOrder(final Set<String> regions) {
        final List<String> order = new ArrayList<>();
        final ConfigurationSection categories = config.getConfigurationSection("questlog.categories");
        if (categories != null) {
            for (final String key : categories.getKeys(false)) {
                final String region = categories.getString(key + ".region");
                if (region != null && regions.contains(region) && !order.contains(region)) {
                    order.add(region);
                }
            }
        }
        regions.stream().filter(region -> !order.contains(region)).sorted().forEach(order::add);
        return order;
    }

    /**
     * Orders quests by quest line: each quest is followed by the quests that need it.
     */
    private List<Map.Entry<String, QuestLogQuest>> questLines(final Map<String, QuestLogQuest> quests) {
        final List<Map.Entry<String, QuestLogQuest>> ordered = new ArrayList<>();
        quests.entrySet().stream()
                .filter(entry -> entry.getValue().prereqs().stream().noneMatch(quests::containsKey))
                .forEach(root -> addQuestLine(quests, root, ordered));
        quests.entrySet().stream().filter(entry -> !ordered.contains(entry)).forEach(ordered::add);
        return ordered;
    }

    private void addQuestLine(final Map<String, QuestLogQuest> quests, final Map.Entry<String, QuestLogQuest> quest,
                              final List<Map.Entry<String, QuestLogQuest>> ordered) {
        if (ordered.contains(quest)) {
            return;
        }
        ordered.add(quest);
        quests.entrySet().stream()
                .filter(next -> next.getValue().prereqs().contains(quest.getKey()))
                .forEach(next -> addQuestLine(quests, next, ordered));
    }

    /**
     * Logs each quest whose category is not configured or whose prerequisite is not a quest, once.
     */
    public void check() {
        for (final Map.Entry<QuestLogIdentifier, QuestLogQuest> entry : questLog.getQuests().getValues().entrySet()) {
            final QuestLogQuest quest = entry.getValue();
            if (config.getConfigurationSection("questlog.categories." + quest.category()) == null) {
                report(entry.getKey().getFull() + ": category '" + quest.category() + "' is not in questlog.categories");
            }
            quest.prereqs().stream().filter(id -> questLog.getQuests().get(id) == null)
                    .forEach(id -> report(entry.getKey().getFull() + ": prereq '" + id + "' is not a quest"));
        }
    }

    private void report(final String problem) {
        if (reported.add(problem)) {
            log.warn(problem);
        }
    }

    private static List<String> wrap(final String text) {
        final List<String> lines = new ArrayList<>();
        final StringBuilder line = new StringBuilder();
        for (final String word : text.split(" ")) {
            if (!line.isEmpty() && line.length() + 1 + word.length() > WRAP) {
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
     * The journal's views.
     */
    private enum Filter {
        /**
         * Started and not finished, tracked first.
         */
        ACTIVE("Active", "the quests you are on"),
        /**
         * The main story of every region.
         */
        MAIN("Main", "the main story of each region"),
        /**
         * Side quests of every region.
         */
        SIDE("Side", "side quests"),
        /**
         * Today's dailies.
         */
        DAILIES("Dailies", "your dailies for today"),
        /**
         * What is done, by region.
         */
        COMPLETED("Completed", "what you have finished");

        /**
         * The label on the filter line.
         */
        private final String label;

        /**
         * What the filter shows, for its hover.
         */
        private final String hint;

        Filter(final String label, final String hint) {
            this.label = label;
            this.hint = hint;
        }
    }

    /**
     * What kind of quest a category holds.
     */
    private enum Type {
        /**
         * The main story.
         */
        MAIN("main story"),
        /**
         * Side quests.
         */
        SIDE("side quest"),
        /**
         * Repeating daily quests.
         */
        DAILY("daily"),
        /**
         * War quests, shown while active.
         */
        WAR("war quest"),
        /**
         * The tutorial, shown with the main story.
         */
        TUTORIAL("tutorial");

        /**
         * How the type reads in a quest's details.
         */
        private final String label;

        Type(final String label) {
            this.label = label;
        }
    }

    /**
     * Where a category's quests are listed.
     *
     * @param region the region heading
     * @param type   the kind of quest
     */
    private record Category(String region, Type type) {
    }

    /**
     * Lines of one body block, left-aligned, each with its own click and hover.
     */
    private static final class Block {

        /**
         * The lines with their widths.
         */
        private final List<Drawn.Line> lines = new ArrayList<>();

        private void add(final String line, final Style style) {
            lines.add(Drawn.Line.of(line, style));
        }

        private void heading(final String line) {
            lines.add(Drawn.heading(line));
        }

        private boolean isEmpty() {
            return lines.isEmpty();
        }

        private void addTo(final List<DialogBody> body) {
            body.add(DialogBody.plainMessage(Drawn.block(lines, TEXT_WIDTH), BLOCK_WIDTH));
        }
    }
}
