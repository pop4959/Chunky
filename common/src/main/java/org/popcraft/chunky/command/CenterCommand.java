package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.Input;

import java.util.Optional;

import static org.popcraft.chunky.Chunky.translate;

public class CenterCommand extends ChunkyCommand {
    public CenterCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        Optional<Double> newX = Optional.empty();
        if (args.length > 1) {
            newX = Input.tryDoubleSuffixed(args[1]);
        }
        Optional<Double> newZ = Optional.empty();
        if (args.length > 2) {
            newZ = Input.tryDoubleSuffixed(args[2]);
        }
        if (!newX.isPresent() || !newZ.isPresent()) {
            sender.sendMessage("help_center");
            return;
        }
        if (Math.abs(newX.get().intValue()) > 3e7 || Math.abs(newZ.get().intValue()) > 3e7) {
            sender.sendMessage("help_center");
            return;
        }
        Selection selection = chunky.getSelection();
        selection.centerX = newX.get().intValue();
        selection.centerZ = newZ.get().intValue();
        sender.sendMessage("format_center", translate("prefix"), selection.centerX, selection.centerZ);
    }
}
