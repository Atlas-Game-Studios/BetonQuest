package org.betonquest.betonquest.compatibility.npc.simplenpcs;

import com.ags.simplenpcs.objects.SNPC;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * FancyNpcs Compatibility Adapter for general BetonQuest NPC behaviour.
 */
public class SimpleAdapter implements org.betonquest.betonquest.api.quest.npc.Npc<SNPC> {
    /**
     * The FancyNpcs NPC instance.
     */
    private final SNPC npc;

    /**
     * Create a new FancyNpcs NPC Adapter.
     *
     * @param npc the FancyNpcs NPC instance
     */
    public SimpleAdapter(final SNPC npc) {
        this.npc = npc;
    }

    @Override
    public void teleport(final Location location) {
        npc.teleportToLocation(location);
    }

    @Override
    public void spawn(final Location location) {
        npc.spawn(location);
    }

    @Override
    public void despawn() {
        npc.despawn();

    }

    @Override
    public void show(final OnlineProfile onlineProfile) {
        Bukkit.getScheduler().runTaskAsynchronously(BetonQuest.getInstance(), () -> {
            final Player player = onlineProfile.getPlayer();
            final boolean isVisible = npc.isShownToPlayer(player);
            if (!isVisible) npc.show(player);
        });
    }

    @Override
    public void hide(final OnlineProfile onlineProfile) {
        Bukkit.getScheduler().runTaskAsynchronously(BetonQuest.getInstance(), () -> {
            final Player player = onlineProfile.getPlayer();
            final boolean isVisible = npc.isShownToPlayer(player);
            if (isVisible) npc.hide(player);
        });
    }

    @Override
    public SNPC getOriginal() {
        return npc;
    }

    @Override
    public String getName() {
        return npc.getName().getFullName();
    }

    @Override
    public String getFormattedName() {
        return getName();
    }

    @Override
    public Optional<Location> getLocation() {
        return Optional.of(npc.getBukkitLocation().clone());
    }

    @Override
    public Optional<Location> getEyeLocation() {
        return Optional.of(npc.getBukkitLocation().clone().add(0, 1, 0));
    }

    @Override
    public boolean isSpawned() {
        return npc.isLoaded();
    }
}
