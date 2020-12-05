package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.Optional;

import static org.popcraft.chunky.Chunky.translate;

public class RadiusCommand extends ChunkyCommand {
    public RadiusCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("help_radius");
            return;
        }
        Optional<Integer> newRadiusX = Input.tryInteger(args[1]);
        if (!newRadiusX.isPresent() || newRadiusX.get() < 0 || newRadiusX.get() > 3e7) {
            sender.sendMessage("help_radius");
            return;
        }
        Selection selection = chunky.getSelection();
        if (args.length > 2) {
            Optional<Integer> newRadiusZ = Input.tryInteger(args[2]);
            if (!newRadiusZ.isPresent() || newRadiusZ.get() < 0 || newRadiusZ.get() > 3e7) {
                sender.sendMessage("help_radius");
                return;
            }
            selection.radiusZ = newRadiusZ.get();
        }
        selection.radiusX = newRadiusX.get();
        if (args.length == 2) {
            selection.radiusZ = selection.radiusX;
            sender.sendMessage("format_radius", translate("prefix"), selection.radiusX);
        } else {
            sender.sendMessage("format_radii", translate("prefix"), selection.radiusX, selection.radiusZ);
        }
    }
}
