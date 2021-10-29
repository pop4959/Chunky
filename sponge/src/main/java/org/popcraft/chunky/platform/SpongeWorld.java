package org.popcraft.chunky.platform;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.platform.util.Vector3;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SpongeWorld implements World {
    private final ServerWorld world;
    private final Border worldBorder;

    public SpongeWorld(ServerWorld world) {
        this.world = world;
        this.worldBorder = new SpongeBorder(world);
    }

    @Override
    public String getName() {
        return world.key().asString();
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return world.hasChunk(x, 0, z);
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(int x, int z) {
        world.loadChunk(x, 0, z, true);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public UUID getUUID() {
        return world.uniqueId();
    }

    @Override
    public int getSeaLevel() {
        return world.seaLevel();
    }

    @Override
    public Location getSpawn() {
        Vector3i spawn = world.properties().spawnPosition();
        return new Location(this, spawn.x(), spawn.y(), spawn.z(), 0, 0);
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
    }

    @Override
    public int getElevation(int x, int z) {
        return world.highestYAt(x, z);
    }

    @Override
    public void playEffect(Player player, String effect) {
        final Location location = player.getLocation();
        final Vector3d vector = Vector3d.from(location.getX(), location.getY(), location.getZ());
        RegistryKey.of(RegistryTypes.PARTICLE_TYPE, ResourceKey.resolve(effect)).asDefaultedReference(Sponge::game).find().ifPresent(particleType -> world.spawnParticles(ParticleEffect.builder().type(particleType).build(), vector));
    }

    @Override
    public void playSound(Player player, String sound) {
        final Location location = player.getLocation();
        try {
            //noinspection PatternValidation
            world.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER, 2f, 1f), location.getX(), location.getY(), location.getZ());
        } catch (final InvalidKeyException ignored) {
        }
    }

    @Override
    public Optional<Path> getEntitiesDirectory() {
        return getDirectory("entities");
    }

    @Override
    public Optional<Path> getPOIDirectory() {
        return getDirectory("poi");
    }

    @Override
    public Optional<Path> getRegionDirectory() {
        return getDirectory("region");
    }

    private Optional<Path> getDirectory(final String name) {
        if (name == null) {
            return Optional.empty();
        }
        Path regionDirectory = world.directory().resolve(name);
        return Files.exists(regionDirectory) ? Optional.of(regionDirectory) : Optional.empty();
    }

    public ServerWorld getWorld() {
        return world;
    }
}
