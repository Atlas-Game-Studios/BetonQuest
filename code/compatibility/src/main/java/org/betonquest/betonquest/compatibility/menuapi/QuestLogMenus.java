package org.betonquest.betonquest.compatibility.menuapi;

import com.ags.menuapi.Menu.ActionMenu;
import com.ags.menuapi.Menu.CallbackMenu;
import com.ags.menuapi.Menu.ListMenu;
import com.ags.menuapi.Menu.Menu;
import com.ags.menuapi.Menu.MenuSize;
import com.ags.menuapi.MenuItem.MenuItem;
import com.ags.menuapi.decoration.Decoration;
import com.ags.menuapi.decoration.Scheme;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.feature.questlog.QuestLog;
import org.betonquest.betonquest.feature.questlog.QuestLogEntry;
import org.betonquest.betonquest.feature.questlog.QuestLogQuest;
import org.betonquest.betonquest.id.questlog.QuestLogIdentifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The quest log menus: a top menu, the active quests and all quests by category.
 * The menu backgrounds are font glyphs from the Atlas resource pack.
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.TooManyMethods"})
public class QuestLogMenus {

    /**
     * Parser for the quest log texts.
     */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Slots of the top menu buttons.
     */
    private static final int ACTIVE_SLOT = 20;

    /**
     * Slot of the all quests button in the top menu.
     */
    private static final int ALL_SLOT = 24;

    /**
     * Slot of the backpack button in the top menu.
     */
    private static final int BACKPACK_SLOT = 40;

    /**
     * First slot of quest lists.
     */
    private static final int FIRST_SLOT = 10;

    /**
     * Last slot of quest lists.
     */
    private static final int LAST_SLOT = 44;

    /**
     * Custom model data of a tracked quest.
     */
    private static final int TRACKED_MODEL = 33;

    /**
     * Custom model data of an untracked quest.
     */
    private static final int UNTRACKED_MODEL = 32;

    /**
     * The plugin the menus belong to.
     */
    private final JavaPlugin plugin;

    /**
     * The config to read the categories from.
     */
    private final ConfigAccessor config;

    /**
     * The quest log to show.
     */
    private final QuestLog questLog;

    /**
     * The profile provider to get profiles of players.
     */
    private final ProfileProvider profileProvider;

    /**
     * Create the quest log menus.
     *
     * @param plugin          the plugin the menus belong to
     * @param config          the config to read the categories from
     * @param questLog        the quest log to show
     * @param profileProvider the profile provider to get profiles of players
     */
    public QuestLogMenus(final JavaPlugin plugin, final ConfigAccessor config, final QuestLog questLog,
                         final ProfileProvider profileProvider) {
        this.plugin = plugin;
        this.config = config;
        this.questLog = questLog;
        this.profileProvider = profileProvider;
    }

    /**
     * Opens the top menu.
     *
     * @param player the player
     */
    public void openTop(final Player player) {
        final CallbackMenu menu = new CallbackMenu(plugin, "", MenuSize.FOURFIVE, 1,
                new Decoration(MenuSize.FOURFIVE, Material.AIR, Scheme.FILL), "׋");
        menu.setCallbackHandler((clicked, page, item, slot, clicker, event) -> {
            switch (slot) {
                case ACTIVE_SLOT -> openActive(clicker, menu);
                case ALL_SLOT -> openCategories(clicker, menu);
                case BACKPACK_SLOT -> clicker.performCommand("backpack");
                default -> {
                }
            }
        });
        menu.addItemToPage(0, ACTIVE_SLOT, new MenuItem(button("<!i>Active Quests", 2)));
        menu.addItemToPage(0, ALL_SLOT, new MenuItem(button("<!i>All Quests", 3)));
        menu.addItemToPage(0, BACKPACK_SLOT, new MenuItem(button("<!i>Backpack", 36)));
        menu.open(player);
    }

    private void openActive(final Player player, final Menu back) {
        final OnlineProfile profile = profileProvider.getProfile(player);
        final CallbackMenu menu = new CallbackMenu(plugin, "", MenuSize.FIVEFOUR, 1,
                new Decoration(MenuSize.FIVEFOUR, Material.AIR, Scheme.BOX), "׍");
        menu.setBackMenu(back);
        final Map<Integer, String> slots = new HashMap<>();
        menu.setCallbackHandler((clicked, page, item, slot, clicker, event) -> {
            final String quest = slots.get(page.getPagenumber() * 100 + slot);
            if (quest == null || event.getClick() != ClickType.RIGHT) {
                return;
            }
            if (questLog.getTracked(profile).contains(quest)) {
                questLog.untrack(profile, quest);
            } else if (!questLog.track(profile, quest)) {
                clicker.sendActionBar(MINI_MESSAGE.deserialize("<red>[!] You are already tracking the maximum amount of quests!"));
                menu.close(clicker);
                return;
            }
            menu.addItemToPage(page.getPagenumber(), slot, new MenuItem(activeIcon(profile, quest)));
        });
        final List<String> quests = new ArrayList<>(new TreeMap<>(questLog.getEntries(profile)).entrySet().stream()
                .filter(entry -> !entry.getValue().complete() && activeIcon(profile, entry.getKey()) != null)
                .map(Map.Entry::getKey).toList());
        fill(menu, quests.size(), (page, slot, index) -> {
            slots.put(page * 100 + slot, quests.get(index));
            menu.addItemToPage(page, slot, new MenuItem(activeIcon(profile, quests.get(index))));
        });
        menu.open(player);
    }

    private void openCategories(final Player player, final Menu back) {
        final ActionMenu menu = new ActionMenu(plugin, "", MenuSize.FIVEFOUR, 1,
                new Decoration(MenuSize.FIVEFOUR, Material.AIR, Scheme.FILL), "׌");
        menu.setBackMenu(back);
        for (final Map<?, ?> category : config.getMapList("questlog.categories")) {
            final int slot = number(category, "slot", -1);
            if (slot < 0) {
                continue;
            }
            final String categoryId = String.valueOf(category.get("id"));
            menu.addItemToPage(0, slot, new MenuItem((page, clicker, event) -> openCategory(clicker, menu, categoryId),
                    button(String.valueOf(category.get("name")), number(category, "model", 0))));
        }
        menu.open(player);
    }

    private int number(final Map<?, ?> map, final String key, final int fallback) {
        return map.get(key) instanceof final Number number ? number.intValue() : fallback;
    }

    private void openCategory(final Player player, final Menu back, final String category) {
        final OnlineProfile profile = profileProvider.getProfile(player);
        final ListMenu menu = new ListMenu(plugin, "", MenuSize.FIVEFOUR, 1,
                new Decoration(MenuSize.FIVEFOUR, Material.AIR, Scheme.BOX), "׎");
        menu.setBackMenu(back);
        final List<Map.Entry<String, QuestLogQuest>> quests = questLines(category);
        fill(menu, quests.size(), (page, slot, index) ->
                menu.addItemToPage(page, slot, new MenuItem(questIcon(profile, quests.get(index).getValue(), quests.get(index).getKey()))));
        menu.open(player);
    }

    /**
     * Lists the quests of a category ordered by quest lines: each quest is followed by the quests that need it.
     *
     * @param category the category
     * @return the quests by full identifier
     */
    private List<Map.Entry<String, QuestLogQuest>> questLines(final String category) {
        final Map<String, QuestLogQuest> inCategory = new TreeMap<>();
        for (final Map.Entry<QuestLogIdentifier, QuestLogQuest> entry : questLog.getQuests().getValues().entrySet()) {
            if (entry.getValue().category().equals(category)) {
                inCategory.put(entry.getKey().getFull(), entry.getValue());
            }
        }
        final List<Map.Entry<String, QuestLogQuest>> ordered = new ArrayList<>();
        inCategory.entrySet().stream()
                .filter(entry -> entry.getValue().prereq() == null || questLog.getQuests().get(entry.getValue().prereq()) == null)
                .forEach(root -> addQuestLine(inCategory, root, ordered));
        return ordered;
    }

    private void addQuestLine(final Map<String, QuestLogQuest> quests, final Map.Entry<String, QuestLogQuest> quest,
                              final List<Map.Entry<String, QuestLogQuest>> ordered) {
        if (ordered.contains(quest)) {
            return;
        }
        ordered.add(quest);
        quests.entrySet().stream()
                .filter(next -> quest.getKey().equals(next.getValue().prereq()))
                .forEach(next -> addQuestLine(quests, next, ordered));
    }

    private void fill(final Menu menu, final int count, final SlotConsumer consumer) {
        int slot = FIRST_SLOT;
        int page = 0;
        for (int index = 0; index < count; index++) {
            while (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
            }
            if (slot > LAST_SLOT) {
                page++;
                menu.addPage();
                slot = FIRST_SLOT;
            }
            consumer.accept(page, slot, index);
            slot++;
        }
    }

    @Nullable
    private ItemStack activeIcon(final OnlineProfile profile, final String quest) {
        final QuestLogQuest definition = questLog.getQuests().get(quest);
        final QuestLogEntry entry = questLog.getEntries(profile).get(quest);
        final QuestLogQuest.Stage stage = definition == null || entry == null ? null : definition.stages().get(entry.stage());
        if (stage == null) {
            return null;
        }
        final boolean tracking = questLog.getTracked(profile).contains(quest);
        final List<Component> lore = new ArrayList<>();
        lore.add(Component.space());
        lore.add(MINI_MESSAGE.deserialize("<!i><u><aqua>" + stage.name() + ":"));
        lore.add(Component.space());
        QuestLog.wrap(stage.objective()).forEach(line -> lore.add(MINI_MESSAGE.deserialize("<!i><white>" + line)));
        lore.add(Component.space());
        if (tracking) {
            lore.add(MINI_MESSAGE.deserialize("<!i><yellow>[Right-Click to untrack]"));
        } else if (questLog.getTracked(profile).size() < QuestLog.MAX_TRACKED) {
            lore.add(MINI_MESSAGE.deserialize("<!i><yellow>[Right-Click to track]"));
        } else {
            lore.add(MINI_MESSAGE.deserialize("<!i><red>[Already tracking max amount of quests]"));
        }
        final ItemStack item = button("<!i><gold>" + definition.name(), tracking ? TRACKED_MODEL : UNTRACKED_MODEL);
        item.editMeta(meta -> meta.lore(lore));
        return item;
    }

    private ItemStack questIcon(final OnlineProfile profile, final QuestLogQuest quest, final String questId) {
        final Status status = Status.forEntry(questLog.getEntries(profile).get(questId));
        final List<Component> lore = new ArrayList<>();
        lore.add(MINI_MESSAGE.deserialize("<!i>" + status.text));
        if (quest.prereq() != null) {
            final QuestLogQuest prereq = questLog.getQuests().get(quest.prereq());
            lore.add(MINI_MESSAGE.deserialize("<!i><dark_aqua>Prerequisite: <gray>" + (prereq == null ? quest.prereq() : prereq.name())));
        }
        if (quest.npc() != null) {
            lore.add(Component.space());
            lore.add(MINI_MESSAGE.deserialize("<!i><gray>To start, talk to " + quest.npc()));
        }
        final ItemStack item = new ItemStack(status.material);
        item.editMeta(meta -> {
            meta.displayName(MINI_MESSAGE.deserialize("<!i>" + status.color + quest.name()));
            meta.lore(lore);
        });
        return item;
    }

    private ItemStack button(final String name, final int model) {
        final ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        final ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        meta.displayName(MINI_MESSAGE.deserialize(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(model);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * How far a player is in a quest.
     */
    private enum Status {
        /**
         * The quest is complete.
         */
        COMPLETED(Material.KNOWLEDGE_BOOK, "<dark_green>", "<green>Completed!"),
        /**
         * The quest is started.
         */
        IN_PROGRESS(Material.WRITABLE_BOOK, "<gold>", "<yellow>In progress..."),
        /**
         * The quest is not started.
         */
        NOT_STARTED(Material.BOOK, "<dark_red>", "<red>Not yet started...");

        /**
         * The icon.
         */
        private final Material material;

        /**
         * The color of the quest name.
         */
        private final String color;

        /**
         * The status line.
         */
        private final String text;

        Status(final Material material, final String color, final String text) {
            this.material = material;
            this.color = color;
            this.text = text;
        }

        private static Status forEntry(@Nullable final QuestLogEntry entry) {
            if (entry == null) {
                return NOT_STARTED;
            }
            return entry.complete() ? COMPLETED : IN_PROGRESS;
        }
    }

    /**
     * Places the item with the given index at a page and slot.
     */
    @FunctionalInterface
    private interface SlotConsumer {

        /**
         * Places an item.
         *
         * @param page  the page
         * @param slot  the slot
         * @param index the index of the item
         */
        void accept(int page, int slot, int index);
    }
}
