package org.popcraft.chunky.platform.impl.batcher;

import org.popcraft.chunky.platform.Batcher;

public class NOOPBatcher implements Batcher {
    public static final NOOPBatcher INSTANCE = new NOOPBatcher();

    private NOOPBatcher() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void shutdown() {
    }
}
