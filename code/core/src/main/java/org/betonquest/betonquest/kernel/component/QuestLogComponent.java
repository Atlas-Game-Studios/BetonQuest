package org.betonquest.betonquest.kernel.component;

import org.betonquest.betonquest.api.config.quest.QuestPackageManager;
import org.betonquest.betonquest.api.dependency.DependencyProvider;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.api.service.identifier.Identifiers;
import org.betonquest.betonquest.api.service.instruction.Instructions;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;
import org.betonquest.betonquest.data.PlayerDataStorage;
import org.betonquest.betonquest.feature.questlog.QuestLog;
import org.betonquest.betonquest.feature.questlog.QuestLogCommand;
import org.betonquest.betonquest.feature.questlog.QuestLogProcessor;
import org.betonquest.betonquest.id.questlog.QuestLogIdentifier;
import org.betonquest.betonquest.id.questlog.QuestLogIdentifierFactory;
import org.betonquest.betonquest.kernel.ProcessorDataLoader;
import org.betonquest.betonquest.kernel.registry.quest.ActionTypeRegistry;
import org.betonquest.betonquest.lib.dependency.component.AbstractCoreComponent;
import org.betonquest.betonquest.quest.action.questlog.QuestLogActionFactory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * The implementation of {@link AbstractCoreComponent} for the {@link QuestLog}.
 */
public class QuestLogComponent extends AbstractCoreComponent {

    /**
     * Create a new QuestLogComponent.
     */
    public QuestLogComponent() {
        super();
    }

    @Override
    public Set<Class<?>> requires() {
        return Set.of(JavaPlugin.class, PluginManager.class, QuestPackageManager.class, BetonQuestLoggerFactory.class,
                Identifiers.class, Instructions.class, ProcessorDataLoader.class, PlayerDataStorage.class,
                ProfileProvider.class, PlaceholderManager.class, ActionTypeRegistry.class);
    }

    @Override
    public Set<Class<?>> provides() {
        return Set.of(QuestLogIdentifierFactory.class, QuestLogProcessor.class, QuestLog.class);
    }

    @Override
    protected void load(final DependencyProvider dependencyProvider) {
        final JavaPlugin plugin = getDependency(JavaPlugin.class);
        final ProfileProvider profileProvider = getDependency(ProfileProvider.class);

        final QuestLogIdentifierFactory identifierFactory = new QuestLogIdentifierFactory(getDependency(QuestPackageManager.class));
        getDependency(Identifiers.class).register(QuestLogIdentifier.class, identifierFactory);
        final QuestLogProcessor processor = new QuestLogProcessor(getDependency(BetonQuestLoggerFactory.class).create(QuestLogProcessor.class),
                getDependency(Instructions.class), identifierFactory);
        getDependency(ProcessorDataLoader.class).addProcessor(processor);

        final QuestLog questLog = new QuestLog(plugin, processor, getDependency(PlayerDataStorage.class), profileProvider,
                getDependency(PlaceholderManager.class));
        getDependency(PluginManager.class).registerEvents(questLog, plugin);
        getDependency(ActionTypeRegistry.class).register("questlog", new QuestLogActionFactory(questLog));

        final QuestLogCommand command = new QuestLogCommand(questLog, profileProvider);
        for (final String name : new String[]{"questlog", "sqj"}) {
            plugin.getCommand(name).setExecutor(command);
            plugin.getCommand(name).setTabCompleter(command);
        }

        dependencyProvider.take(QuestLogIdentifierFactory.class, identifierFactory);
        dependencyProvider.take(QuestLogProcessor.class, processor);
        dependencyProvider.take(QuestLog.class, questLog);
    }
}
