package org.popcraft.chunky.mixin;

import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public interface ChunkHolderAccess {
    @Accessor("pendingFullStateConfirmation")
    CompletableFuture<?> chunky$getPendingFullStateConfirmation();
}
