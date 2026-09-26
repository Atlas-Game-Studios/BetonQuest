package org.betonquest.betonquest.quest.action.teleport;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.action.OnlineAction;
import org.betonquest.betonquest.api.service.conversation.Conversations;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Teleports the player to specified location.
 */
public class TeleportAction implements OnlineAction {

    /**
     * Conversation API.
     */
    private final Conversations conversations;

    /**
     * Location to teleport to.
     */
    private final Argument<Location> location;

    /**
     * Create a new teleport action that teleports the player to the given location.
     *
     * @param conversations the Conversation API
     * @param location      location to teleport to
     */
    public TeleportAction(final Conversations conversations, final Argument<Location> location) {
        this.conversations = conversations;
        this.location = location;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        conversations.cancel(profile);
        final Location playerLocation = location.getValue(profile);
        final Player player = profile.getPlayer();
        final Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            player.teleportAsync(playerLocation);
        } else {
            teleportWithPassengers(vehicle, playerLocation);
        }
    }

    /**
     * Atlas: entities with passengers can't be teleported, so eject, move and re-mount them.
     *
     * @param entity   the entity to teleport
     * @param location the target location
     */
    private static void teleportWithPassengers(final Entity entity, final Location location) {
        final List<Entity> passengers = entity.getPassengers();
        entity.eject();
        entity.teleportAsync(location).thenAccept(success -> passengers.forEach(passenger -> {
            teleportWithPassengers(passenger, location);
            entity.addPassenger(passenger);
        }));
    }

    @Override
    public boolean isPrimaryThreadEnforced() {
        return true;
    }
}
