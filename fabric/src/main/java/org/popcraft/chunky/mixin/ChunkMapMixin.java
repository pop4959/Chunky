package org.popcraft.chunky.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnnecessaryInterfaceModifier")
@Mixin(ChunkMap.class)
public interface ChunkMapMixin {
    @Invoker("getVisibleChunkIfPresent")
    public ChunkHolder invokeGetVisibleChunkIfPresent(long pos);

    @Invoker("readChunk")
    public CompletableFuture<Optional<CompoundTag>> invokeReadChunk(ChunkPos pos);

    @Accessor("pendingUnloads")
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> getPendingUnloads();
}
