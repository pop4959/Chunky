package org.popcraft.chunky.trim;

import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.TranslationKey;

public record InhabitedTimeFilter(long minTicks, long maxTicks) implements TrimFilter {
    public InhabitedTimeFilter(final long maxTicks) {
        this(0, maxTicks);
    }

    @Override
    public boolean test(final TrimChunk chunk) {
        return chunk.inhabitedTime() >= minTicks && chunk.inhabitedTime() <= maxTicks;
    }

    @Override
    public boolean readsChunkData() {
        return true;
    }

    @Override
    public void sendNotice(final Sender sender) {
        if (minTicks <= 0) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_TRIM_CONFIRM_INHABITED, Formatting.number(maxTicks));
        } else if (maxTicks == Long.MAX_VALUE) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_TRIM_CONFIRM_INHABITED_MINIMUM, Formatting.number(minTicks));
        } else {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_TRIM_CONFIRM_INHABITED_RANGE, Formatting.number(minTicks), Formatting.number(maxTicks));
        }
    }
}
