package org.popcraft.chunky.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.popcraft.chunky.ChunkyNeoForge;
import org.popcraft.chunky.ducks.MinecraftServerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtension {
    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();

    @Unique
    private final AtomicBoolean chunky$needChunkSystemHousekeeping = new AtomicBoolean(false);

    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickConnection()V"))
    private void tickPaused(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        this.chunky$runChunkSystemHousekeeping(booleanSupplier);
    }

    @Override
    public void chunky$runChunkSystemHousekeeping(BooleanSupplier haveTime) {
        if (this.chunky$needChunkSystemHousekeeping.compareAndSet(true, false)) {
            for (ServerLevel level : this.getAllLevels()) {
                level.getChunkSource().chunkMap.tick(haveTime);
                if (!ChunkyNeoForge.ENABLE_MOONRISE_WORKAROUNDS) {
                    // note: Moonrise destroys the vanilla entity system, so skip it here if it's present
                    level.entityManager.tick();
                }
            }
        }
    }

    @Override
    public void chunky$markChunkSystemHousekeeping() {
        this.chunky$needChunkSystemHousekeeping.set(true);
    }
}
