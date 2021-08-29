package org.popcraft.chunky.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import org.popcraft.chunky.ChunkyForge;
import org.popcraft.chunky.util.Coordinate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ForgeWorld implements World {
    private static final TicketType<Unit> CHUNKY = TicketType.create(ChunkyForge.MODID, (unit, unit2) -> 0);
    private final ServerLevel world;
    private final Border worldBorder;

    public ForgeWorld(ServerLevel world) {
        this.world = world;
        this.worldBorder = new ForgeBorder(world.getWorldBorder());
    }

    @Override
    public String getName() {
        return world.dimension().location().toString();
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        if (Thread.currentThread() != world.getServer().getRunningThread()) {
            return CompletableFuture.supplyAsync(() -> isChunkGenerated(x, z), world.getServer()).join();
        } else {
            final ChunkPos chunkPos = new ChunkPos(x, z);
            ChunkMap chunkStorage = world.getChunkSource().chunkMap;
            ChunkHolder loadedChunkHolder = chunkStorage.getVisibleChunkIfPresent(chunkPos.toLong());
            if (loadedChunkHolder != null && loadedChunkHolder.getLastAvailableStatus() == ChunkStatus.FULL) {
                return true;
            }
            ChunkHolder unloadedChunkHolder = chunkStorage.pendingUnloads.get(chunkPos.toLong());
            if (unloadedChunkHolder != null && unloadedChunkHolder.getLastAvailableStatus() == ChunkStatus.FULL) {
                return true;
            }
            CompoundTag chunkNbt;
            try {
                chunkNbt = chunkStorage.readChunk(chunkPos);
            } catch (IOException e) {
                return false;
            }
            if (chunkNbt != null && chunkNbt.contains("Level", 10)) {
                CompoundTag levelCompoundTag = chunkNbt.getCompound("Level");
                if (levelCompoundTag.contains("Status", 8)) {
                    return "full".equals(levelCompoundTag.getString("Status"));
                }
            }
            return false;
        }
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(int x, int z) {
        if (Thread.currentThread() != world.getServer().getRunningThread()) {
            return CompletableFuture.supplyAsync(() -> getChunkAtAsync(x, z), world.getServer()).join();
        } else {
            final CompletableFuture<Void> chunkFuture = new CompletableFuture<>();
            final ChunkPos chunkPos = new ChunkPos(x, z);
            world.getChunkSource().addRegionTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE);
            world.getChunkSource().runDistanceManagerUpdates();
            ChunkMap chunkManager = world.getChunkSource().chunkMap;
            ChunkHolder chunkHolder = chunkManager.getVisibleChunkIfPresent(chunkPos.toLong());
            if (chunkHolder == null) {
                chunkFuture.complete(null);
                world.getChunkSource().removeRegionTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE);
            } else {
                chunkHolder.getOrScheduleFuture(ChunkStatus.FULL, chunkManager).thenAcceptAsync(either -> {
                    chunkFuture.complete(null);
                    world.getChunkSource().removeRegionTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE);
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
        BlockPos spawnPoint = world.getSharedSpawnPos();
        return new Coordinate(spawnPoint.getX(), spawnPoint.getZ());
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
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
        Path directory = DimensionType.getStorageFolder(world.dimension(), world.getServer().getWorldPath(LevelResource.ROOT).toFile()).toPath().normalize().resolve(name);
        return Files.exists(directory) ? Optional.of(directory) : Optional.empty();
    }
}
