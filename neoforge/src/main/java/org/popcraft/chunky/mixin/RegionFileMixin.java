package org.popcraft.chunky.mixin;

import net.minecraft.world.level.chunk.storage.RegionFile;
import org.popcraft.chunky.ChunkyNeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RegionFile.class)
public abstract class RegionFileMixin {
    @ModifyVariable(method = "<init>(Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Ljava/nio/file/Path;Ljava/nio/file/Path;Lnet/minecraft/world/level/chunk/storage/RegionFileVersion;Z)V",
                    at = @At(value = "LOAD"),
                    argsOnly = true,
                    require = 0)
    private boolean syncWrites(final boolean sync) {
        return sync && ChunkyNeoForge.SYNC_CHUNK_WRITES;
    }
}
