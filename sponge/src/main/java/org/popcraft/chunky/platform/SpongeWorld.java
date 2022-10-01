package org.popcraft.chunky.platform;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.popcraft.chunky.platform.util.Location;
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

    public SpongeWorld(final ServerWorld world) {
        this.world = world;
        this.worldBorder = new SpongeBorder(world);
    }

    @Override
    public String getName() {
        return world.key().asString();
    }

    @Override
    public String getKey() {
        return getName();
    }

    @Override
    public boolean isChunkGenerated(final int x, final int z) {
        return world.hasChunk(x, 0, z);
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(final int x, final int z) {
        return CompletableFuture.allOf(CompletableFuture.completedFuture(world.loadChunk(x, 0, z, true)));
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
        final Vector3i spawn = world.properties().spawnPosition();
        return new Location(this, spawn.x(), spawn.y(), spawn.z(), 0, 0);
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
    }

    @Override
    public int getElevation(final int x, final int z) {
        return world.highestYAt(x, z) + 1;
    }

    @Override
    public int getMaxElevation() {
        return world.maximumHeight();
    }

    @Override
    public void playEffect(final Player player, final String effect) {
        final Location location = player.getLocation();
        final Vector3d vector = Vector3d.from(location.getX(), location.getY(), location.getZ());
        RegistryKey.of(RegistryTypes.PARTICLE_TYPE, ResourceKey.resolve(effect)).asDefaultedReference(Sponge::game).find().ifPresent(particleType -> world.spawnParticles(ParticleEffect.builder().type(particleType).build(), vector));
    }

    @Override
    public void playSound(final Player player, final String sound) {
        final Key soundKey;
        try {
            //noinspection PatternValidation
            soundKey = Key.key(sound);
        } catch (final InvalidKeyException e) {
            return;
        }
        final Location location = player.getLocation();
        world.playSound(Sound.sound(soundKey, Sound.Source.MASTER, 2f, 1f), location.getX(), location.getY(), location.getZ());

    }

    @Override
    public Optional<Path> getDirectory(final String name) {
        if (name == null) {
            return Optional.empty();
        }
        final Path regionDirectory = world.directory().resolve(name);
        return Files.exists(regionDirectory) ? Optional.of(regionDirectory) : Optional.empty();
    }

    public ServerWorld getWorld() {
        return world;
    }
}
