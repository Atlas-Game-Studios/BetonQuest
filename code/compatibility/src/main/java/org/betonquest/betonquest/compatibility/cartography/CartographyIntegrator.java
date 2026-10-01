package org.betonquest.betonquest.compatibility.cartography;

import com.ags.cartography.api.CartographyService;
import com.ags.cartography.objects.Waypoint;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.feature.questlog.QuestLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Shows quest log stages as Cartography quest waypoints.
 */
public class CartographyIntegrator implements Integration {

    /**
     * Create a new Cartography integrator.
     */
    public CartographyIntegrator() {
    }

    @Override
    public void enable(final BetonQuestApi api) {
        // Empty
    }

    @Override
    public void postEnable(final BetonQuestApi api) throws QuestException {
        final CartographyService cartography = Bukkit.getServicesManager().load(CartographyService.class);
        if (cartography == null) {
            throw new QuestException("Cartography does not provide its service");
        }
        BetonQuest.getInstance().getComponentLoader().get(QuestLog.class).setWaypoints(new QuestLog.Waypoints() {
            @Override
            public void add(final Player player, final String compass) {
                cartography.addWaypoint(player, Waypoint.WaypointType.QUEST, compass);
            }

            @Override
            public void remove(final Player player, final String compass) {
                cartography.removeWaypoint(player, Waypoint.WaypointType.QUEST, compass);
            }

            @Override
            public void track(final Player player, final String compass, final String color, final String icon) {
                cartography.trackWaypoint(player, compass, color, icon);
            }

            @Override
            public void untrack(final Player player, final String compass) {
                cartography.untrackWaypoint(player, compass);
            }
        });
    }

    @Override
    public void disable() {
        // Empty
    }
}
