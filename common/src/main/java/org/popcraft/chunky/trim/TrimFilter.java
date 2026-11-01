package org.popcraft.chunky.trim;

import org.popcraft.chunky.platform.Sender;

public interface TrimFilter {
    boolean test(TrimChunk chunk);

    default boolean readsChunkData() {
        return false;
    }

    void sendNotice(Sender sender);
}
