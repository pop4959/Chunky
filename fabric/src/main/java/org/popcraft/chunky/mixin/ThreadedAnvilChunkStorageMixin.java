package org.popcraft.chunky.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnnecessaryInterfaceModifier")
@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {
    @Invoker("getChunkHolder")
    public abstract ChunkHolder invokeGetChunkHolder(long pos);

    @Invoker("getUpdatedChunkNbt")
    public abstract CompletableFuture<Optional<NbtCompound>> invokeGetUpdatedChunkNbt(ChunkPos pos);

    @Accessor("chunksToUnload")
    public abstract Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunksToUnload();

    // Block entity tickers memory leak fixed by removing the unloaded chunk's block entity tickers from the world,
    // avoiding the memory leak as well as increasing tick performance relatively to block entities.
    // If we don't remove the block entity tickers, they will get accumulated in the world while chunks get generated.
    @Inject(method = "method_18843(Lnet/minecraft/server/world/ChunkHolder;Ljava/util/concurrent/CompletableFuture;JLnet/minecraft/world/chunk/Chunk;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;unloadEntities(Lnet/minecraft/world/chunk/WorldChunk;)V"))
    private void unloadBlockEntityTickers(ChunkHolder chunkHolder, CompletableFuture<Chunk> completableFuture, long l, Chunk chunk, CallbackInfo ci) {
        ((WorldChunk) chunk).getWorld().blockEntityTickers.removeAll(((WorldChunk) chunk).blockEntityTickers.values());
    }
}
