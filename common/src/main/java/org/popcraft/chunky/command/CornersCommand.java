package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.Input;

import java.util.Optional;

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
        Optional<Double> x1 = Input.tryDoubleSuffixed(args[1]);
        Optional<Double> z1 = Input.tryDoubleSuffixed(args[2]);
        Optional<Double> x2 = Input.tryDoubleSuffixed(args[3]);
        Optional<Double> z2 = Input.tryDoubleSuffixed(args[4]);
        if (!x1.isPresent() || !z1.isPresent() || !x2.isPresent() || !z2.isPresent()) {
            sender.sendMessage("help_corners");
            return;
        }
        if (Input.isPastWorldLimit(x1.get()) || Input.isPastWorldLimit(z1.get()) || Input.isPastWorldLimit(x2.get()) || Input.isPastWorldLimit(z2.get())) {
            sender.sendMessage("help_corners");
            return;
        }
        double centerX = (x1.get() + x2.get()) / 2d;
        double centerZ = (z1.get() + z2.get()) / 2d;
        double radiusX = Math.abs(x1.get() - x2.get()) / 2d;
        double radiusZ = Math.abs(z1.get() - z2.get()) / 2d;
        chunky.getSelection().center(centerX, centerZ).radiusX(radiusX).radiusZ(radiusZ);
        sender.sendMessagePrefixed("format_center", Formatting.number(centerX), Formatting.number(centerZ));
        String shape;
        if (radiusX == radiusZ) {
            sender.sendMessagePrefixed("format_radius", Formatting.number(radiusX));
            shape = ShapeType.SQUARE;
        } else {
            sender.sendMessagePrefixed("format_radii", Formatting.number(radiusX), Formatting.number(radiusZ));
            shape = ShapeType.RECTANGLE;
        }
        chunky.getSelection().shape(shape);
        sender.sendMessagePrefixed("format_shape", shape);
    }
}
