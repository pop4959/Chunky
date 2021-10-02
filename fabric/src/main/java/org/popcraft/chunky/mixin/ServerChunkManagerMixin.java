package org.popcraft.chunky.mixin;

import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerMixin {
    @Invoker("tick")
    @SuppressWarnings({"UnusedReturnValue", "UnnecessaryInterfaceModifier"})
    public boolean tick();
}
