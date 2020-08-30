package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Input;
import org.popcraft.chunky.Selection;

import java.util.Optional;

public class RadiusCommand extends ChunkyCommand {
    public RadiusCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chunky.message("help_radius"));
            return;
        }
        Optional<Integer> newRadius = Input.tryInteger(args[1]);
        if (!newRadius.isPresent()) {
            sender.sendMessage(chunky.message("help_radius"));
            return;
        }
        if (newRadius.get() < 0 || newRadius.get() > 3e7) {
            sender.sendMessage(chunky.message("help_radius"));
            return;
        }
        Selection selection = chunky.getSelection();
        selection.radius = newRadius.get();
        sender.sendMessage(chunky.message("format_radius", selection.radius));
    }
}
