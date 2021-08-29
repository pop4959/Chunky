package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.iterator.PatternType;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PatternCommand extends ChunkyCommand {
    public PatternCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TranslationKey.HELP_PATTERN);
            return;
        }
        Optional<String> inputPattern = Input.tryPattern(args[1]);
        if (!inputPattern.isPresent()) {
            sender.sendMessage(TranslationKey.HELP_PATTERN);
            return;
        }
        String pattern = inputPattern.get();
        chunky.getSelection().pattern(pattern);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_PATTERN, pattern);
    }

    @Override
    public List<String> tabSuggestions(Sender sender, String[] args) {
        if (args.length == 2) {
            return PatternType.ALL;
        }
        return Collections.emptyList();
    }
}
