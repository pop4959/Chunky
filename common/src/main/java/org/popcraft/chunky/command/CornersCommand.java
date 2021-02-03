package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.Optional;

import static org.popcraft.chunky.Chunky.translate;

public class CornersCommand extends ChunkyCommand {
    public CornersCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("help_corners");
            return;
        }
        Optional<Integer> x1 = Input.tryIntegerSuffixed(args[1]);
        Optional<Integer> z1 = Input.tryIntegerSuffixed(args[2]);
        Optional<Integer> x2 = Input.tryIntegerSuffixed(args[3]);
        Optional<Integer> z2 = Input.tryIntegerSuffixed(args[4]);
        if (!x1.isPresent() || !z1.isPresent() || !x2.isPresent() || !z2.isPresent()) {
            sender.sendMessage("help_corners");
            return;
        }
        if (Math.abs(x1.get()) > 3e7 || Math.abs(z1.get()) > 3e7 || Math.abs(x2.get()) > 3e7 || Math.abs(z2.get()) > 3e7) {
            sender.sendMessage("help_corners");
            return;
        }
        Selection selection = chunky.getSelection();
        selection.centerX = Math.floorDiv(x1.get() + x2.get(), 2);
        selection.centerZ = Math.floorDiv(z1.get() + z2.get(), 2);
        selection.radiusX = (int) Math.ceil(Math.abs(x1.get() - x2.get()) / 2f);
        selection.radiusZ = (int) Math.ceil(Math.abs(z1.get() - z2.get()) / 2f);
        sender.sendMessage("format_center", translate("prefix"), selection.centerX, selection.centerZ);
        if (selection.radiusX == selection.radiusZ) {
            sender.sendMessage("format_radius", translate("prefix"), selection.radiusX);
            selection.shape = "square";
        } else {
            sender.sendMessage("format_radii", translate("prefix"), selection.radiusX, selection.radiusZ);
            selection.shape = "rectangle";
        }
        sender.sendMessage("format_shape", translate("prefix"), selection.shape);
    }
}
