package org.popcraft.chunky.mixin;

import net.minecraft.server.MinecraftServer;
import org.popcraft.chunky.ChunkyProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow private int emptyTicks;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void preventPausing(CallbackInfo ci) {
        if (!ChunkyProvider.get().getGenerationTasks().isEmpty()) {
            this.emptyTicks = 0;
        }
    }

}
