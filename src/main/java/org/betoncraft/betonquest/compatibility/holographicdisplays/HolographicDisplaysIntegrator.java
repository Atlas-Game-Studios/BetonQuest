package org.betoncraft.betonquest.compatibility.holographicdisplays;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betoncraft.betonquest.compatibility.Compatibility;
import org.betoncraft.betonquest.compatibility.Integrator;
import org.betoncraft.betonquest.compatibility.citizens.CitizensHologram;
import org.betoncraft.betonquest.compatibility.simplenpcs.SimpleNPCsHologram;


@SuppressWarnings("PMD.CommentRequired")
public class HolographicDisplaysIntegrator implements Integrator {

    private static HolographicDisplaysIntegrator instance;
    private HologramLoop hologramLoop;

    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public HolographicDisplaysIntegrator() {
        instance = this;
    }

    @Override
    public void hook() {
        hologramLoop = new HologramLoop();

        // if Citizens is hooked, start CitizensHologram
        if (Compatibility.getHooked().contains("Citizens")) {
            new CitizensHologram();
        }

        // if SimpleNPCs is hooked start SimpleNPCsHologram
        if (Compatibility.getHooked().contains("SimpleNPCs")) {
            new SimpleNPCsHologram();
        }
    }

    @Override
    public void reload() {
        if (instance.hologramLoop != null) {
            instance.hologramLoop.cancel();
            instance.hologramLoop = new HologramLoop();
        }
    }

    @Override
    public void close() {
        if (instance.hologramLoop != null) {
            hologramLoop.cancel();
        }
    }

}
