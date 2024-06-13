package org.popcraft.chunky.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnnecessaryInterfaceModifier")
@Mixin(ServerChunkLoadingManager.class)
public interface ServerChunkLoadingManagerMixin {
    @Invoker("getChunkHolder")
    public ChunkHolder invokeGetChunkHolder(long pos);

    @Invoker("getUpdatedChunkNbt")
    public CompletableFuture<Optional<NbtCompound>> invokeGetUpdatedChunkNbt(ChunkPos pos);

    @Accessor("chunksToUnload")
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunksToUnload();
}
