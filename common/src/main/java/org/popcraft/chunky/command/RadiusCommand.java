package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
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
        Optional<Integer> newRadiusX = Input.tryIntegerSuffixed(args[1]);
        if (!newRadiusX.isPresent() || newRadiusX.get() < 0 || Input.isPastWorldLimit(newRadiusX.get())) {
            sender.sendMessage("help_radius");
            return;
        }
        int radiusX = newRadiusX.get();
        if (args.length > 2) {
            Optional<Integer> newRadiusZ = Input.tryIntegerSuffixed(args[2]);
            if (!newRadiusZ.isPresent() || newRadiusZ.get() < 0 || Input.isPastWorldLimit(newRadiusZ.get())) {
                sender.sendMessage("help_radius");
                return;
            }
            int radiusZ = newRadiusZ.get();
            chunky.getSelection().radiusX(radiusX).radiusZ(radiusZ);
            sender.sendMessage("format_radii", translate("prefix"), radiusX, radiusZ);
        } else {
            chunky.getSelection().radius(radiusX);
            sender.sendMessage("format_radius", translate("prefix"), radiusX);
        }
    }
}
