package org.popcraft.chunky.platform;

import io.papermc.paper.entity.TeleportFlag;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.platform.util.Location;

import java.util.List;
import java.util.UUID;

import static org.popcraft.chunky.util.Translator.translateKey;

public class BukkitPlayer extends BukkitSender implements Player {
    private final JavaPlugin plugin = JavaPlugin.getPlugin(ChunkyBukkit.class);

    final org.bukkit.entity.Player player;

    public BukkitPlayer(final org.bukkit.entity.Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public World getWorld() {
        return new BukkitWorld(player.getWorld());
    }

    @Override
    public Location getLocation() {
        final org.bukkit.Location location = player.getLocation();
        return new Location(getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public void teleport(final Location location) {
        final org.bukkit.World world = Bukkit.getWorld(location.getWorld().getName());
        final org.bukkit.Location loc = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        final Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            player.teleportAsync(loc);
        } else if (player.getWorld().equals(loc.getWorld())) {
            vehicle.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE);
        } else if (!plugin.getServer().isOwnedByCurrentRegion(player.getLocation())) {
            player.getScheduler().execute(plugin, () -> teleport(location), null, 1);
        } else {
            final List<Entity> passengers = vehicle.getPassengers();
            vehicle.getScheduler().execute(plugin, vehicle::eject, null, 1);
            player.teleportAsync(loc).thenAcceptAsync(ignored -> {
                vehicle.teleportAsync(loc);
                for (final Entity passenger : passengers) {
                    passenger.teleportAsync(loc);
                    if (passenger instanceof final org.bukkit.entity.Player playerPassenger) {
                        playerPassenger.hideEntity(plugin, vehicle);
                        playerPassenger.showEntity(plugin, vehicle);
                    }
                    vehicle.getScheduler().execute(plugin, () -> vehicle.addPassenger(passenger), null, 1);
                }
            }, command -> player.getScheduler().execute(plugin, command, null, 1));
        }
    }

    @Override
    public void sendActionBar(final String key) {
        final String message = formatColored(translateKey(key, false));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
