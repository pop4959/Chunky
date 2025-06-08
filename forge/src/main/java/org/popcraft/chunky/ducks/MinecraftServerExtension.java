package org.popcraft.chunky.ducks;

import java.util.function.BooleanSupplier;

public interface MinecraftServerExtension {
    void chunky$runChunkSystemHousekeeping(BooleanSupplier haveTime);

    void chunky$markChunkSystemHousekeeping();
}
