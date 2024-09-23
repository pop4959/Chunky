package org.popcraft.chunky.mixin;

import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheMixin {
    @Invoker("getChunkFutureMainThread")
    public CompletableFuture<ChunkResult<ChunkAccess>> invokeGetChunkFutureMainThread(final int chunkX, final int chunkZ,
                                                                                      final ChunkStatus toStatus,
                                                                                      final boolean create);
}
