package org.popcraft.chunky.trim;

import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.TranslationKey;

import java.util.concurrent.TimeUnit;

import static org.popcraft.chunky.util.Translator.translate;

public record UnmodifiedFilter(double amount, TimeUnit unit, long cutoff) implements TrimFilter {
    @Override
    public boolean test(final TrimChunk chunk) {
        if (!chunk.exists()) {
            return true;
        }
        return chunk.lastModified() != 0 && chunk.lastModified() <= cutoff;
    }

    @Override
    public void sendNotice(final Sender sender) {
        sender.sendMessagePrefixed(TranslationKey.FORMAT_TRIM_CONFIRM_UNMODIFIED, translate(unitKey(), Formatting.number(amount)));
    }

    private String unitKey() {
        final boolean singular = amount == 1;
        return switch (unit) {
            case MINUTES -> singular ? TranslationKey.TIME_MINUTE : TranslationKey.TIME_MINUTES;
            case HOURS -> singular ? TranslationKey.TIME_HOUR : TranslationKey.TIME_HOURS;
            case DAYS -> singular ? TranslationKey.TIME_DAY : TranslationKey.TIME_DAYS;
            default -> singular ? TranslationKey.TIME_SECOND : TranslationKey.TIME_SECONDS;
        };
    }
}
