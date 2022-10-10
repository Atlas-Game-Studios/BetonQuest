package org.betonquest.betonquest.api.bukkit.config.custom.multi.fallback;

import org.betonquest.betonquest.api.bukkit.config.custom.fallback.FallbackConfiguration;
import org.betonquest.betonquest.api.bukkit.config.custom.multi.MultiConfigurationSectionConfiguration;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * This is a combination of {@link FallbackConfiguration} and {@link MultiConfigurationSectionConfiguration}.
 * It can be used to have all features of both interfaces.
 * Calls to methods of {@link MultiConfigurationSectionConfiguration}
 * will be forwarded to the original instance, passed in the constructor.
 */
public class MultiFallbackConfiguration extends FallbackConfiguration implements MultiConfigurationSectionConfiguration {
    /**
     * The original instance held as {@link MultiConfigurationSectionConfiguration}.
     */
    private final MultiConfigurationSectionConfiguration original;

    /**
     * Creates a new decorator instance.
     *
     * @param original The original {@link ConfigurationSection} that should be decorated.
     * @param fallback The fallback {@link ConfigurationSection} that should be used
     *                 when there is no value in the  original {@link ConfigurationSection}.
     * @param <T>      a Class extending {@link Configuration} and {@link MultiConfigurationSectionConfiguration}
     */
    public <T extends Configuration & MultiConfigurationSectionConfiguration>
    MultiFallbackConfiguration(@NotNull final T original, @Nullable final ConfigurationSection fallback) {
        super(original, fallback);
        this.original = original;
    }

    @Override
    public boolean needSave() {
        return original.needSave();
    }

    @Override
    public Set<ConfigurationSection> getUnsavedConfigs() {
        return original.getUnsavedConfigs();
    }

    @Override
    public boolean markAsSaved(final ConfigurationSection section) {
        return original.markAsSaved(section);
    }

    @Override
    public ConfigurationSection getSourceConfigurationSection(final String path) throws InvalidConfigurationException {
        return original.getSourceConfigurationSection(path);
    }

    @Override
    public List<String> getUnassociatedKeys() {
        return original.getUnassociatedKeys();
    }

    @Override
    public void associateWith(final ConfigurationSection targetConfig) {
        original.associateWith(targetConfig);
    }

    @Override
    public void associateWith(final String path, final ConfigurationSection targetConfig) {
        original.associateWith(path, targetConfig);
    }
}
