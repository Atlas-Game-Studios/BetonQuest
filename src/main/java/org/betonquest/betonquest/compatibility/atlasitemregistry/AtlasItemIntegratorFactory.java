package org.betonquest.betonquest.compatibility.atlasitemregistry;

import org.betonquest.betonquest.compatibility.Integrator;
import org.betonquest.betonquest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link AtlasItemIntegrator} instances.
 */
public class AtlasItemIntegratorFactory implements IntegratorFactory {
    @Override
    public Integrator getIntegrator() {
        return new AtlasItemIntegrator();
    }
}
