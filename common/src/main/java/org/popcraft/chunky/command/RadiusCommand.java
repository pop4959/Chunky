package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
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
        final Optional<Integer> signX = Input.trySign(args[1]);
        final Optional<Double> newRadiusX = Input.tryDoubleSuffixed(signX.isPresent() ? args[1].substring(1) : args[1]);
        if (!newRadiusX.isPresent() || newRadiusX.get() < 0 || Input.isPastWorldLimit(newRadiusX.get())) {
            sender.sendMessage("help_radius");
            return;
        }
        final Selection current = chunky.getSelection().build();
        double radiusX = signX.map(sign -> current.radiusX() + sign * newRadiusX.get()).orElseGet(newRadiusX::get);
        if (radiusX < 0 || Input.isPastWorldLimit(radiusX)) {
            sender.sendMessage("help_radius");
            return;
        }
        if (args.length > 2) {
            Optional<Integer> signZ = Input.trySign(args[2]);
            Optional<Double> newRadiusZ = Input.tryDoubleSuffixed(signZ.isPresent() ? args[2].substring(1) : args[2]);
            if (!newRadiusZ.isPresent() || newRadiusZ.get() < 0 || Input.isPastWorldLimit(newRadiusZ.get())) {
                sender.sendMessage("help_radius");
                return;
            }
            final double radiusZ = signZ.map(sign -> current.radiusZ() + sign * newRadiusZ.get()).orElseGet(newRadiusZ::get);
            if (radiusZ < 0 || Input.isPastWorldLimit(radiusZ)) {
                sender.sendMessage("help_radius");
                return;
            }
            chunky.getSelection().radiusX(radiusX).radiusZ(radiusZ);
            sender.sendMessagePrefixed("format_radii", Formatting.number(radiusX), Formatting.number(radiusZ));
        } else {
            chunky.getSelection().radius(radiusX);
            sender.sendMessagePrefixed("format_radius", Formatting.number(radiusX));
        }
    }
}
