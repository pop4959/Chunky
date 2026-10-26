package org.popcraft.chunky.mixin;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {
    @Unique
    private static final int BOOSTED_UNLOAD_LEVEL = 16;

    @ModifyArg(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder$LevelChangeListener;onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"), index = 2, require = 0)
    private int boostUnloadedPriority(int newLevel) {
        if (newLevel > ChunkLevel.MAX_LEVEL) {
            newLevel = BOOSTED_UNLOAD_LEVEL; // boost priority on unloaded chunks to avoid queue buildup
        }
        return newLevel;
    }
}
