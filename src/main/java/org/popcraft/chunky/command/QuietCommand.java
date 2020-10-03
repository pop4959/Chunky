package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.Selection;

import java.util.Optional;

public class QuietCommand extends ChunkyCommand {
    public QuietCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        Optional<Integer> newQuiet = Optional.empty();
        if (args.length > 1) {
            newQuiet = Input.tryInteger(args[1]);
        }
        if (!newQuiet.isPresent()) {
            sender.sendMessage(chunky.message("help_quiet"));
            return;
        }
        Selection selection = chunky.getSelection();
        selection.quiet = Math.max(0, newQuiet.get());
        sender.sendMessage(chunky.message("format_quiet", chunky.message("prefix"), selection.quiet));
    }
}
