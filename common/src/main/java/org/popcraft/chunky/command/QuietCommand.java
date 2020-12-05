package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.Optional;

import static org.popcraft.chunky.Chunky.translate;

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
            sender.sendMessage("help_quiet");
            return;
        }
        Selection selection = chunky.getSelection();
        selection.quiet = Math.max(0, newQuiet.get());
        sender.sendMessage("format_quiet", translate("prefix"), selection.quiet);
    }
}
