package org.popcraft.chunky.platform;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.platform.util.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.popcraft.chunky.util.Translator.translateKey;

public class BukkitPlayer extends BukkitSender implements Player {
    private static final boolean ACTION_BAR_SUPPORTED;

    static {
        boolean barSupported;
        try {
            org.bukkit.entity.Player.class.getMethod("spigot");
            barSupported = true;
        } catch (NoSuchMethodException e) {
            barSupported = false;
        }
        ACTION_BAR_SUPPORTED = barSupported;
    }

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
        final Vehicle vehicle = (Vehicle) player.getVehicle();
        if (vehicle != null) {
            final List<Entity> passengers = new ArrayList<>(vehicle.getPassengers());
            vehicle.eject();
            runTask(() -> teleportAsync(vehicle, loc).thenAccept(vehicleTpResult -> {
                if (Boolean.TRUE.equals(vehicleTpResult)) {
                    runTask(() -> passengers.forEach(passenger -> {
                                teleportAsync(passenger, loc).thenAccept(passengerTpResult -> {
                                    if (Boolean.TRUE.equals(passengerTpResult)) {
                                        if (passenger instanceof org.bukkit.entity.Player) {
                                            refreshEntity((org.bukkit.entity.Player) passenger, vehicle);
                                        }
                                        runTask(() -> vehicle.addPassenger(passenger), 1);
                                    }
                                });
                                runTask(() -> {
                                    if (passenger instanceof org.bukkit.entity.Player) {
                                        refreshEntity((org.bukkit.entity.Player) passenger, vehicle);
                                    }
                                }, 1);
                            }
                    ), 1);
                }
            }), 1);
        } else {
            teleportAsync(player, loc);
        }
    }

    private CompletableFuture<Boolean> teleportAsync(final Entity entity, final org.bukkit.Location location) {
        if (Paper.isPaper()) {
            return Paper.teleportAsync(entity, location);
        } else {
            return CompletableFuture.completedFuture(entity.teleport(location));
        }
    }

    private static void runTask(final Runnable runnable, final long ticks) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ChunkyBukkit.class), runnable, ticks);
    }

    public static void refreshEntity(final org.bukkit.entity.Player player, final org.bukkit.entity.Entity entity) {
        player.hideEntity(JavaPlugin.getPlugin(ChunkyBukkit.class), entity);
        player.showEntity(JavaPlugin.getPlugin(ChunkyBukkit.class), entity);
    }

    @Override
    public void sendActionBar(final String key) {
        if (ACTION_BAR_SUPPORTED) {
            final String message = formatColored(translateKey(key, false));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } else {
            this.sendMessage(key);
        }
    }
}
