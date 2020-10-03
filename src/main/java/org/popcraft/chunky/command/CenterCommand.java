package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.Selection;

import java.util.Optional;

public class CenterCommand extends ChunkyCommand {
    public CenterCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        Optional<Double> newX = Optional.empty();
        if (args.length > 1) {
            newX = Input.tryDouble(args[1]);
        }
        Optional<Double> newZ = Optional.empty();
        if (args.length > 2) {
            newZ = Input.tryDouble(args[2]);
        }
        if (!newX.isPresent() || !newZ.isPresent()) {
            sender.sendMessage(chunky.message("help_center"));
            return;
        }
        if (Math.abs(newX.get().intValue()) > 3e7 || Math.abs(newZ.get().intValue()) > 3e7) {
            sender.sendMessage(chunky.message("help_center"));
            return;
        }
        Selection selection = chunky.getSelection();
        selection.centerX = newX.get().intValue();
        selection.centerZ = newZ.get().intValue();
        sender.sendMessage(chunky.message("format_center", chunky.message("prefix"), selection.centerX, selection.centerZ));
    }
}
