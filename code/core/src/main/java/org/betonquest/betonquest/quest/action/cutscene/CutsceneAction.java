package org.betonquest.betonquest.quest.action.cutscene;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.identifier.ActionIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.action.PlayerAction;
import org.betonquest.betonquest.api.service.action.ActionManager;
import org.betonquest.betonquest.conversation.menu.MenuConvIOFactory;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Plays a timed sequence of lines, commands and actions to a player.
 * <p>
 * Lines are shown like conversation screens, in the conversation style's colors, when the default
 * conversation IO is a menu IO with its own renderer (e.g. MythicHUD), otherwise they are sent to chat
 * with their own colors. Narration lines without a speaker keep their own colors either way.
 * A line's delay starts once its typewriter effect is done, which right-clicking skips.
 * The sequence stops when the player goes offline.
 */
public class CutsceneAction implements PlayerAction {

    /**
     * Parser for the legacy {@code &} formatted texts.
     */
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    /**
     * The plugin instance used for scheduling.
     */
    private final Plugin plugin;

    /**
     * The action manager to run action steps.
     */
    private final ActionManager actionManager;

    /**
     * The registry to find the default conversation IO in.
     */
    private final ConversationIORegistry conversationIORegistry;

    /**
     * The config with the default conversation IO.
     */
    private final ConfigAccessor config;

    /**
     * The steps to play in order.
     */
    private final List<Step> steps;

    /**
     * Create a new cutscene action.
     *
     * @param plugin                 the plugin instance used for scheduling
     * @param actionManager          the action manager to run action steps
     * @param conversationIORegistry the registry to find the default conversation IO in
     * @param config                 the config with the default conversation IO
     * @param steps                  the steps to play in order
     */
    public CutsceneAction(final Plugin plugin, final ActionManager actionManager, final ConversationIORegistry conversationIORegistry,
                          final ConfigAccessor config, final List<Step> steps) {
        this.plugin = plugin;
        this.actionManager = actionManager;
        this.conversationIORegistry = conversationIORegistry;
        this.config = config;
        this.steps = steps;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        play(profile, MenuConvIOFactory.defaultScreen(conversationIORegistry, config), 0, -1);
    }

    private void play(final Profile profile, @Nullable final MenuConvIOFactory screen, final int index,
                      final long lastLine) throws QuestException {
        final Optional<OnlineProfile> online = profile.getOnlineProfile();
        if (online.isEmpty()) {
            return;
        }
        if (index >= steps.size()) {
            // Only our own line: an io:dialogue line fired by the last actions must stay for its duration
            if (screen != null && lastLine >= 0) {
                screen.hideLine(online.get(), lastLine);
            }
            return;
        }
        final Step step = steps.get(index);
        final long[] shownLine = {lastLine};
        final Runnable next = () -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                play(profile, screen, index + 1, shownLine[0]);
            } catch (final QuestException e) {
                plugin.getLogger().warning("Cutscene step " + (index + 2) + " failed: " + e.getMessage());
            }
        }, step.delayTicks);
        if (step.msg != null && screen != null) {
            final boolean narration = step.speaker == null;
            shownLine[0] = screen.showLine(online.get(), narration ? Component.empty() : plain(step.speaker.getValue(profile)),
                    narration ? LEGACY.deserialize(step.msg.getValue(profile)) : plain(step.msg.getValue(profile)), next);
            return;
        }
        runStep(profile, online.get(), step);
        next.run();
    }

    /**
     * Drops the legacy colors so the conversation style formats the text, like in conversations.
     *
     * @param legacy the legacy {@code &} formatted text
     * @return the plain text
     */
    private Component plain(final String legacy) {
        return Component.text(PlainTextComponentSerializer.plainText().serialize(LEGACY.deserialize(legacy)));
    }

    private void runStep(final Profile profile, final OnlineProfile online, final Step step) throws QuestException {
        if (step.msg != null) {
            final Component text = LEGACY.deserialize(step.msg.getValue(profile));
            online.getPlayer().sendMessage(step.speaker == null ? text
                    : LEGACY.deserialize(step.speaker.getValue(profile)).append(Component.text(": ")).append(text));
        } else if (step.cmd != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), step.cmd.getValue(profile));
        } else if (step.actions != null) {
            actionManager.run(profile, step.actions.getValue(profile));
        }
    }

    @Override
    public boolean isPrimaryThreadEnforced() {
        return true;
    }

    /**
     * One cutscene step. At most one of msg, cmd and actions is set; none means a pause.
     *
     * @param speaker    legacy {@code &} formatted name of who says the msg, null for narration
     * @param msg        legacy {@code &} formatted line
     * @param cmd        console command
     * @param actions    actions to run
     * @param delayTicks ticks to wait before the next step, after the line is typed
     */
    public record Step(@Nullable Argument<String> speaker, @Nullable Argument<String> msg, @Nullable Argument<String> cmd,
                       @Nullable Argument<List<ActionIdentifier>> actions, long delayTicks) {
    }
}
