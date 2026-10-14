package org.popcraft.chunky.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskDispatcher;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@Mixin(ChunkMap.class)
public class ChunkMapPatchMixin {
    @Shadow
    @Final
    private ChunkTaskDispatcher worldgenTaskDispatcher;

    @WrapWithCondition(method = "onLevelChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkTaskDispatcher;onLevelChange(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/IntSupplier;ILjava/util/function/IntConsumer;)V"), require = 0) // require = 0 because Moonrise
    private boolean suppressRaceCondition(ChunkTaskDispatcher instance, ChunkPos pos, IntSupplier oldLevel, int newLevel, IntConsumer setQueueLevel) {
        // updating levels for both dispatchers result in a race condition in level updating, because two queues race
        // the same level field for updating, resulting in a corrupted state that leaves tasks not updated.
        // plus, lighting is usually not a bottleneck, so drop lighting priority changes won't hurt much,
        // allowing boosted priority in ChunkHolderMixin to work properly.
        return instance == this.worldgenTaskDispatcher;
    }
}
