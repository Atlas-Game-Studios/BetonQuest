/**
 * BetonQuest - advanced quests for Bukkit
 * Copyright (C) 2015  Jakub "Co0sh" Sapalski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.betonquest.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.config.Config;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.utils.Debug;
import pl.betoncraft.betonquest.utils.PlayerConverter;

/**
 * Handler for global locations.
 * 
 * @author Co0sh
 */
public class GlobalLocations extends BukkitRunnable {

    private List<GlobalLocation> locations = new ArrayList<GlobalLocation>();
    private final List<GlobalLocation> finalLocations;
    private static GlobalLocations instance;

    /**
     * Creates new instance of global locations handler.
     */
    public GlobalLocations() {
        instance = this;
        // get list of global locations and make it final
        for (String packName : Config.getPackageNames()) {
            ConfigPackage pack = Config.getPackage(packName);
            String rawGlobalLocations = pack.getString("main.global_locations");
            if (rawGlobalLocations == null || rawGlobalLocations.equals("")) {
                continue;
            }
            String[] parts = rawGlobalLocations.split(",");
            for (String objective : parts) {
                GlobalLocation gL = new GlobalLocation(pack, objective);
                if (gL.isValid())
                    locations.add(gL);
            }
        }
        finalLocations = locations;
    }

    /**
     * Stops active global locations timer
     */
    public static void stop() {
        instance.cancel();
    }

    @Override
    public void run() {
        // do nothing if there is no defined locations
        if (finalLocations == null) {
            this.cancel();
            return;
        }
        // loop all online players
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player : players) {
            // for each player loop all available locations
            locations: for (GlobalLocation location : finalLocations) {
                // if location is not set, stop everything, there is an error in
                // config
                if (location.getLocation() == null) {
                    continue locations;
                }
                // if player is inside location, do stuff
                if (player.getLocation().getWorld().equals(location.getLocation().getWorld())
                    && player.getLocation().distanceSquared(
                            new Location(
                                    location.getLocation().getWorld(), location.getLocation()
                                    .getX(), location.getLocation().getY(), location.getLocation()
                                    .getZ())
                            ) <= location.getDistance()*location.getDistance()) {
                    // check if player has already triggered this location
                    if (BetonQuest.getInstance().getDBHandler(PlayerConverter.getID(player))
                            .hasTag("global_" + location.getLabel())) {
                        continue locations;
                    }
                    // check all conditions
                    if (location.getConditions() != null) {
                        for (String condition : location.getConditions()) {
                            if (!BetonQuest.condition(PlayerConverter.getID(player), condition)) {
                                // if some conditions are not met, skip to next location
                                continue locations;
                            }
                        }
                    }
                    // set the tag, player has triggered this location
                    BetonQuest.getInstance().getDBHandler(PlayerConverter.getID(player))
                            .addTag("global_" + location.getLabel());
                    // fire all events for the location
                    for (String event : location.getEvents()) {
                        BetonQuest.event(PlayerConverter.getID(player), event);
                    }
                }
            }
        }
    }

    /**
     * Represents single global location.
     * 
     * @author Co0sh
     */
    private class GlobalLocation {

        private Location location;
        private String[] conditions;
        private String[] events;
        private int distance;
        private String label;
        private boolean valid = true;

        /**
         * Creates new global location using objective event's ID.
         * 
         * @param event
         *            ID of the event
         */
        public GlobalLocation(ConfigPackage pack, String objective) {
            Debug.info("Creating new GlobalLocation from " + pack.getName() + "." + objective + " event.");
            label = objective;
            String instructions = pack.getString("objectives." + objective);
            if (instructions == null || !instructions.startsWith("location ")) {
                Debug.error("Location objective not found in objective " + objective);
                valid = false;
                return;
            }
            // check amount of arguments in event's instruction
            String[] parts = instructions.split(" ");
            if (parts.length < 2) {
                Debug.error("There is an error in global location's objective " + objective);
                valid = false;
                return;
            }
            // check amount of arguments in location definition
            String[] rawLocation = parts[1].split(";");
            if (rawLocation.length != 5) {
                Debug.error("Wrong location format in global location's objective " + objective);
                valid = false;
                return;
            }
            double x = 0, y = 0, z = 0;
            // get x, y and z; check if they are valid numbers
            try {
                x = Double.parseDouble(rawLocation[0]);
                y = Double.parseDouble(rawLocation[1]);
                z = Double.parseDouble(rawLocation[2]);
                distance = Integer.parseInt(rawLocation[4]);
            } catch (NumberFormatException e) {
                Debug.error("Wrong argument in location definition in global location's objective "
                    + objective);
                valid = false;
                return;
            }
            if (Bukkit.getWorld(rawLocation[3]) == null) {
                Debug.error("The world doesn't exist in global location's objective " + objective);
                valid = false;
                return;
            }
            location = new Location(Bukkit.getWorld(rawLocation[3]), x, y, z);
            // extract all conditions and events
            for (String part : parts) {
                if (part.contains("conditions:")) {
                    conditions = part.substring(11).split(",");
                    for (int i = 0; i < conditions.length; i++) {
                	if (!conditions[i].contains(".")) {
                	    conditions[i] = pack.getName() + "." + conditions[i];
                	}
                    }
                }
                if (part.contains("events:")) {
                    events = part.substring(7).split(",");
                    for (int i = 0; i < events.length; i++) {
                	if (!events[i].contains(".")) {
                	    events[i] = pack.getName() + "." + events[i];
                	}
                    }
                }
            }
        }

        /**
         * @return the location
         */
        public Location getLocation() {
            return location;
        }

        /**
         * @return the conditions
         */
        public String[] getConditions() {
            return conditions;
        }

        /**
         * @return the events
         */
        public String[] getEvents() {
            return events;
        }

        /**
         * @return the distance
         */
        public int getDistance() {
            return distance;
        }

        /**
         * @return the tag
         */
        public String getLabel() {
            return label;
        }

        public boolean isValid() {
            return valid;
        }
    }
}
