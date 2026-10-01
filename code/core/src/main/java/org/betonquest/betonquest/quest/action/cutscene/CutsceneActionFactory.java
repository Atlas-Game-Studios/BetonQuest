package org.betonquest.betonquest.quest.action.cutscene;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.identifier.ActionIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.quest.action.PlayerAction;
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory;
import org.betonquest.betonquest.api.service.action.ActionManager;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory to create {@link CutsceneAction}s from the {@code cutscenes} section of the action's package.
 * <p>
 * Each cutscene is a list of steps. A step has one of {@code msg} (with an optional {@code speaker}),
 * {@code cmd} or {@code actions}, and an optional {@code delay} in seconds to wait before the next step.
 * A step with only a delay is a pause.
 */
public class CutsceneActionFactory implements PlayerActionFactory {

    /**
     * The plugin instance used for scheduling.
     */
    private final Plugin plugin;

    /**
     * The action manager to run action steps.
     */
    private final ActionManager actionManager;

    /**
     * The config to read the default conversation IO from.
     */
    private final ConfigAccessor config;

    /**
     * The registry to find the default conversation IO in.
     */
    private final ConversationIORegistry conversationIORegistry;

    /**
     * Create a new cutscene action factory.
     *
     * @param plugin                 the plugin instance used for scheduling
     * @param actionManager          the action manager to run action steps
     * @param config                 the config to read the default conversation IO from
     * @param conversationIORegistry the registry to find the default conversation IO in
     */
    public CutsceneActionFactory(final Plugin plugin, final ActionManager actionManager, final ConfigAccessor config,
                                 final ConversationIORegistry conversationIORegistry) {
        this.plugin = plugin;
        this.actionManager = actionManager;
        this.config = config;
        this.conversationIORegistry = conversationIORegistry;
    }

    @Override
    public PlayerAction parsePlayer(final Instruction instruction) throws QuestException {
        final String name = instruction.string().get().getValue(null);
        final ConfigurationSection root = instruction.getPackage().getConfig().getConfigurationSection("cutscenes");
        if (root == null || !root.isList(name)) {
            throw new QuestException("Cutscene '" + name + "' not found in package " + instruction.getPackage().getQuestPath());
        }
        final List<CutsceneAction.Step> steps = new ArrayList<>();
        for (final Map<?, ?> raw : root.getMapList(name)) {
            steps.add(parseStep(instruction, name, steps.size() + 1, raw));
        }
        return new CutsceneAction(plugin, actionManager, conversationIORegistry, config, steps);
    }

    private CutsceneAction.Step parseStep(final Instruction instruction, final String name, final int index,
                                          final Map<?, ?> raw) throws QuestException {
        final long delayTicks = raw.get("delay") instanceof final Number delay ? Math.round(delay.doubleValue() * 20) : 0;
        if (raw.get("msg") != null) {
            return new CutsceneAction.Step(string(instruction, raw.get("speaker")), string(instruction, raw.get("msg")),
                    null, null, delayTicks);
        }
        if (raw.get("cmd") != null) {
            return new CutsceneAction.Step(null, null, string(instruction, raw.get("cmd")), null, delayTicks);
        }
        if (raw.get("actions") instanceof final List<?> actions) {
            final Argument<List<ActionIdentifier>> ids = instruction.chainForArgument(String.join(",", actions.stream().map(Object::toString).toList()))
                    .identifier(ActionIdentifier.class).list().get();
            return new CutsceneAction.Step(null, null, null, ids, delayTicks);
        }
        if (delayTicks > 0) {
            return new CutsceneAction.Step(null, null, null, null, delayTicks);
        }
        throw new QuestException("Cutscene '" + name + "' step " + index + " needs one of msg, cmd, actions or delay");
    }

    @Nullable
    private Argument<String> string(final Instruction instruction, @Nullable final Object raw) throws QuestException {
        return raw == null ? null : instruction.chainForArgument(raw.toString()).string().get();
    }
}
