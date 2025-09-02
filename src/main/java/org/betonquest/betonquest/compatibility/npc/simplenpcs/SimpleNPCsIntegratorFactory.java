package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import org.betonquest.betonquest.compatibility.Integrator;
import org.betonquest.betonquest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link SimpleNPCsIntegrator} instances.
 */
public class SimpleNPCsIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public SimpleNPCsIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new SimpleNPCsIntegrator();
    }
}
