package org.popcraft.chunky.trim;

import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.TranslationKey;

public record InhabitedTimeFilter(long maxTicks) implements TrimFilter {
    @Override
    public boolean test(final TrimChunk chunk) {
        return chunk.inhabitedTime() <= maxTicks;
    }

    @Override
    public boolean readsChunkData() {
        return true;
    }

    @Override
    public void sendNotice(final Sender sender) {
        sender.sendMessagePrefixed(TranslationKey.FORMAT_TRIM_CONFIRM_INHABITED, Formatting.number(maxTicks));
    }
}
