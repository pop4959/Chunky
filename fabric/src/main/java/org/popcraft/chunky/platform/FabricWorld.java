package org.popcraft.chunky.platform;

import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import org.popcraft.chunky.mixin.ServerChunkManagerMixin;
import org.popcraft.chunky.mixin.ThreadedAnvilChunkStorageMixin;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.util.Input;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FabricWorld implements World {
    private static final int TICKING_LOAD_DURATION = Input.tryInteger(System.getProperty("chunky.tickingLoadDuration")).orElse(0);
    private static final ChunkTicketType<Unit> CHUNKY = ChunkTicketType.create("chunky", (unit, unit2) -> 0);
    private static final ChunkTicketType<Unit> CHUNKY_TICKING = ChunkTicketType.create("chunky_ticking", (unit, unit2) -> 0, TICKING_LOAD_DURATION * 20);
    private final ServerWorld serverWorld;
    private final Border worldBorder;

    public FabricWorld(final ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
        this.worldBorder = new FabricBorder(serverWorld.getWorldBorder());
    }

    @Override
    public String getName() {
        return serverWorld.getRegistryKey().getValue().toString();
    }

    @Override
    public String getKey() {
        return getName();
    }

    @Override
    public boolean isChunkGenerated(final int x, final int z) {
        if (Thread.currentThread() != serverWorld.getServer().getThread()) {
            return CompletableFuture.supplyAsync(() -> isChunkGenerated(x, z), serverWorld.getServer()).join();
        } else {
            final ChunkPos chunkPos = new ChunkPos(x, z);
            final ThreadedAnvilChunkStorage chunkStorage = serverWorld.getChunkManager().threadedAnvilChunkStorage;
            final ThreadedAnvilChunkStorageMixin chunkStorageMixin = (ThreadedAnvilChunkStorageMixin) chunkStorage;
            final ChunkHolder loadedChunkHolder = chunkStorageMixin.invokeGetChunkHolder(chunkPos.toLong());
            if (loadedChunkHolder != null && loadedChunkHolder.getCurrentStatus() == ChunkStatus.FULL) {
                return true;
            }
            final ChunkHolder unloadedChunkHolder = chunkStorageMixin.getChunksToUnload().get(chunkPos.toLong());
            if (unloadedChunkHolder != null && unloadedChunkHolder.getCurrentStatus() == ChunkStatus.FULL) {
                return true;
            }
            return chunkStorageMixin.invokeGetUpdatedChunkNbt(chunkPos).join().map(chunkNbt -> chunkNbt.contains("Status", 8) && "full".equals(chunkNbt.getString("Status"))).orElse(false);
        }
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(final int x, final int z) {
        if (Thread.currentThread() != serverWorld.getServer().getThread()) {
            return CompletableFuture.supplyAsync(() -> getChunkAtAsync(x, z), serverWorld.getServer()).join();
        } else {
            final ChunkPos chunkPos = new ChunkPos(x, z);
            final ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
            serverChunkManager.addTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE);
            if (TICKING_LOAD_DURATION > 0) {
                serverChunkManager.addTicket(CHUNKY_TICKING, chunkPos, 1, Unit.INSTANCE);
            }
            ((ServerChunkManagerMixin) serverChunkManager).invokeTick();
            final ThreadedAnvilChunkStorage threadedAnvilChunkStorage = serverChunkManager.threadedAnvilChunkStorage;
            final ThreadedAnvilChunkStorageMixin threadedAnvilChunkStorageMixin = (ThreadedAnvilChunkStorageMixin) threadedAnvilChunkStorage;
            final ChunkHolder chunkHolder = threadedAnvilChunkStorageMixin.invokeGetChunkHolder(chunkPos.toLong());
            final CompletableFuture<Void> chunkFuture = chunkHolder == null ? CompletableFuture.completedFuture(null) : CompletableFuture.allOf(chunkHolder.getChunkAt(ChunkStatus.FULL, threadedAnvilChunkStorage));
            chunkFuture.whenCompleteAsync((ignored, throwable) -> serverChunkManager.removeTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE), serverWorld.getServer());
            return chunkFuture;
        }
    }

    @Override
    public UUID getUUID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSeaLevel() {
        return serverWorld.getSeaLevel();
    }

    @Override
    public Location getSpawn() {
        final BlockPos pos = serverWorld.getSpawnPos();
        final float angle = serverWorld.getSpawnAngle();
        return new Location(this, pos.getX(), pos.getY(), pos.getZ(), angle, 0);
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
    }

    @Override
    public int getElevation(final int x, final int z) {
        final int height = serverWorld.getChunk(x >> 4, z >> 4).sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x, z) + 1;
        final int logicalHeight = serverWorld.getLogicalHeight();
        if (height >= logicalHeight) {
            BlockPos.Mutable pos = new BlockPos.Mutable(x, logicalHeight, z);
            int air = 0;
            while (pos.getY() > serverWorld.getBottomY()) {
                pos = pos.move(Direction.DOWN);
                final BlockState blockState = serverWorld.getBlockState(pos);
                if (blockState.getMaterial().isSolid() && air > 1) {
                    return pos.getY() + 1;
                }
                air = blockState.isAir() ? air + 1 : 0;
            }
        }
        return height;
    }

    @Override
    public int getMaxElevation() {
        return serverWorld.getLogicalHeight();
    }

    @Override
    public void playEffect(final Player player, final String effect) {
        final Location location = player.getLocation();
        final BlockPos pos = new BlockPos(location.getX(), location.getY(), location.getZ());
        Input.tryInteger(effect).ifPresent(eventId -> serverWorld.syncWorldEvent(null, eventId, pos, 0));
    }

    @Override
    public void playSound(final Player player, final String sound) {
        final Location location = player.getLocation();
        serverWorld.getServer()
                .getRegistryManager()
                .getOptional(RegistryKeys.SOUND_EVENT)
                .flatMap(soundEventRegistry -> soundEventRegistry.getOrEmpty(Identifier.tryParse(sound)))
                .ifPresent(soundEvent -> serverWorld.playSound(null, location.getX(), location.getY(), location.getZ(), soundEvent, SoundCategory.MASTER, 2f, 1f));
    }

    @Override
    public Optional<Path> getDirectory(final String name) {
        if (name == null) {
            return Optional.empty();
        }
        final Path directory = DimensionType.getSaveDirectory(serverWorld.getRegistryKey(), serverWorld.getServer().getSavePath(WorldSavePath.ROOT)).normalize().resolve(name);
        return Files.exists(directory) ? Optional.of(directory) : Optional.empty();
    }

    public ServerWorld getServerWorld() {
        return serverWorld;
    }
}
