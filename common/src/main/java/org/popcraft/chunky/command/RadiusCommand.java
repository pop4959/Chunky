package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.Input;

import java.util.Optional;

public class RadiusCommand extends ChunkyCommand {
    public RadiusCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("help_radius");
            return;
        }
        Optional<Double> newRadiusX = Input.tryDoubleSuffixed(args[1]);
        if (!newRadiusX.isPresent() || newRadiusX.get() < 0 || Input.isPastWorldLimit(newRadiusX.get())) {
            sender.sendMessage("help_radius");
            return;
        }
        double radiusX = newRadiusX.get();
        if (args.length > 2) {
            Optional<Double> newRadiusZ = Input.tryDoubleSuffixed(args[2]);
            if (!newRadiusZ.isPresent() || newRadiusZ.get() < 0 || Input.isPastWorldLimit(newRadiusZ.get())) {
                sender.sendMessage("help_radius");
                return;
            }
            double radiusZ = newRadiusZ.get();
            chunky.getSelection().radiusX(radiusX).radiusZ(radiusZ);
            sender.sendMessagePrefixed("format_radii", Formatting.number(radiusX), Formatting.number(radiusZ));
        } else {
            chunky.getSelection().radius(radiusX);
            sender.sendMessagePrefixed("format_radius", Formatting.number(radiusX));
        }
    }
}
