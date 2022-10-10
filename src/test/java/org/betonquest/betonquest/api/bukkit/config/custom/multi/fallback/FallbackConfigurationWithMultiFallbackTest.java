package org.betonquest.betonquest.api.bukkit.config.custom.multi.fallback;

import org.betonquest.betonquest.api.bukkit.config.custom.fallback.FallbackConfiguration;
import org.betonquest.betonquest.api.bukkit.config.custom.fallback.FallbackConfigurationTest;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link FallbackConfiguration} class.
 */
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
public class FallbackConfigurationWithMultiFallbackTest extends FallbackConfigurationTest {
    @Override
    public Configuration getConfig() {
        final Configuration original = YamlConfiguration.loadConfiguration(new File("src/test/resources/api/bukkit/fallback/original.yml"));
        fallback = YamlConfiguration.loadConfiguration(new File("src/test/resources/api/bukkit/fallback/fallback.yml"));

        final Configuration defaults = super.getDefaultConfig().getDefaults();
        assertNotNull(defaults);
        original.setDefaults(defaults);

        return new FallbackConfiguration(original, fallback);
    }
}
