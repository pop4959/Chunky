package org.popcraft.chunky.mixin;

import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheMixin {
    @Invoker("runDistanceManagerUpdates")
    @SuppressWarnings({"UnusedReturnValue", "UnnecessaryInterfaceModifier"})
    public boolean invokeRunDistanceManagerUpdates();
}
