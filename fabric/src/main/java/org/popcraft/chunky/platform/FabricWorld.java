package org.popcraft.chunky.platform;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.popcraft.chunky.mixin.ServerChunkManagerMixin;
import org.popcraft.chunky.mixin.ThreadedAnvilChunkStorageMixin;
import org.popcraft.chunky.util.Coordinate;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FabricWorld implements World {
    private ServerWorld serverWorld;
    private Border worldBorder;
    private static final ChunkTicketType<Unit> CHUNKY = ChunkTicketType.create("chunky", (unit, unit2) -> 0);

    public FabricWorld(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
        this.worldBorder = new FabricBorder(serverWorld.getWorldBorder());
    }

    @Override
    public String getName() {
        return serverWorld.getRegistryKey().getValue().toString();
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return false;
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(int x, int z) {
        if (Thread.currentThread() != serverWorld.getServer().getThread()) {
            return CompletableFuture.supplyAsync(() -> getChunkAtAsync(x, z), serverWorld.getServer()).join();
        } else {
            final CompletableFuture<Void> chunkFuture = new CompletableFuture<>();
            final ChunkPos chunkPos = new ChunkPos(x, z);
            serverWorld.getChunkManager().addTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE);
            ((ServerChunkManagerMixin) serverWorld.getChunkManager()).tick();
            ThreadedAnvilChunkStorage threadedAnvilChunkStorage = serverWorld.getChunkManager().threadedAnvilChunkStorage;
            ThreadedAnvilChunkStorageMixin threadedAnvilChunkStorageMixin = (ThreadedAnvilChunkStorageMixin) threadedAnvilChunkStorage;
            ChunkHolder chunkHolder = threadedAnvilChunkStorageMixin.getChunkHolder(chunkPos.toLong());
            if (chunkHolder == null) {
                chunkFuture.complete(null);
            } else {
                threadedAnvilChunkStorage.getChunk(chunkHolder, ChunkStatus.FULL).thenAcceptAsync(either -> {
                    chunkFuture.complete(null);
                    serverWorld.getChunkManager().removeTicket(CHUNKY, chunkPos, 0, Unit.INSTANCE);
                }, serverWorld.getServer());
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
        return serverWorld.getSeaLevel();
    }

    @Override
    public Coordinate getSpawnCoordinate() {
        BlockPos spawnPos = serverWorld.getSpawnPos();
        return new Coordinate(spawnPos.getX(), spawnPos.getZ());
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return serverWorld.equals(((FabricWorld) o).serverWorld);
    }

    @Override
    public int hashCode() {
        return serverWorld.hashCode();
    }
}
