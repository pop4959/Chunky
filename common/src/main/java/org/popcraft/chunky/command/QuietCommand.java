package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Optional;

public class QuietCommand extends ChunkyCommand {
    public QuietCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        Optional<Integer> newQuiet = Optional.empty();
        if (args.length > 1) {
            newQuiet = Input.tryInteger(args[1]);
        }
        if (!newQuiet.isPresent()) {
            sender.sendMessage(TranslationKey.HELP_QUIET);
            return;
        }
        int quietInterval = Math.max(0, newQuiet.get());
        chunky.getOptions().setQuietInterval(quietInterval);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_QUIET, quietInterval);
    }
}
