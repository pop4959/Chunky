package org.popcraft.chunky.platform;

import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.storage.FolderName;
import org.popcraft.chunky.ChunkyForge;
import org.popcraft.chunky.util.Coordinate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ForgeWorld implements World {
    private ServerWorld world;
    private WorldBorder worldBorder;
    private static final TicketType<Unit> CHUNKY = TicketType.create(ChunkyForge.MODID, (unit, unit2) -> 0);

    public ForgeWorld(ServerWorld world) {
        this.world = world;
        this.worldBorder = world.getWorldBorder();
    }

    @Override
    public String getName() {
        return world.getDimensionKey().getLocation().toString();
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return false;
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(int x, int z) {
        if (Thread.currentThread() != world.getServer().getExecutionThread()) {
            return CompletableFuture.supplyAsync(() -> getChunkAtAsync(x, z), world.getServer()).join();
        } else {
            final CompletableFuture<Void> chunkFuture = new CompletableFuture<>();
            final ChunkPos chunkPos = new ChunkPos(x, z);
            world.getChunkProvider().registerTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE);
            world.getChunkProvider().func_217235_l();
            ChunkManager chunkManager = world.getChunkProvider().chunkManager;
            ChunkHolder chunkHolder = chunkManager.func_219219_b(chunkPos.asLong());
            if (chunkHolder == null) {
                chunkFuture.complete(null);
            } else {
                chunkManager.func_219244_a(chunkHolder, ChunkStatus.FULL).thenAcceptAsync(either -> {
                    chunkFuture.complete(null);
                    world.getChunkProvider().releaseTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE);
                }, world.getServer());
            }
            return chunkFuture;
        }
    }

    @Override
    public UUID getUUID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    @Override
    public Coordinate getSpawnCoordinate() {
        BlockPos spawnPoint = world.getSpawnPoint();
        return new Coordinate(spawnPoint.getX(), spawnPoint.getZ());
    }

    @Override
    public Border getWorldBorder() {
        return new ForgeBorder(worldBorder);
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
        Path directory = DimensionType.getDimensionFolder(world.getDimensionKey(), world.getServer().func_240776_a_(FolderName.DOT).toFile()).toPath().normalize().resolve(name);
        return Files.exists(directory) ? Optional.of(directory) : Optional.empty();
    }
}
