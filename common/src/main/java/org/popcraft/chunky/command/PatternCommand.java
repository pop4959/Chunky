package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.iterator.PatternType;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Parameter;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.popcraft.chunky.util.Translator.translate;

public class PatternCommand extends ChunkyCommand {
    public PatternCommand(final Chunky chunky) {
        super(chunky);
    }

    public void execute(final Sender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TranslationKey.HELP_PATTERN);
            return;
        }
        final Optional<String> optionalType = Input.tryPattern(args[1]);
        if (optionalType.isEmpty()) {
            sender.sendMessage(TranslationKey.HELP_PATTERN);
            return;
        }
        final String type = optionalType.get();
        final String value = args.length > 2 ? args[2] : null;
        if (PatternType.CSV.equals(type) && value == null) {
            sender.sendMessage(TranslationKey.HELP_PATTERN);
            return;
        }
        final Parameter pattern = Parameter.of(type, value);
        chunky.getSelection().pattern(pattern);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_PATTERN, translate("pattern_" + pattern.getType()));
    }

    @Override
    public List<String> tabSuggestions(final String[] args) {
        if (args.length == 2) {
            return PatternType.ALL;
        }
        return Collections.emptyList();
    }
}
