package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
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
        if (Input.isPastWorldLimit(x1.get()) || Input.isPastWorldLimit(z1.get()) || Input.isPastWorldLimit(x2.get()) || Input.isPastWorldLimit(z2.get())) {
            sender.sendMessage("help_corners");
            return;
        }
        int centerX = Math.floorDiv(x1.get() + x2.get(), 2);
        int centerZ = Math.floorDiv(z1.get() + z2.get(), 2);
        int radiusX = (int) Math.ceil(Math.abs(x1.get() - x2.get()) / 2f);
        int radiusZ = (int) Math.ceil(Math.abs(z1.get() - z2.get()) / 2f);
        chunky.getSelection().center(centerX, centerZ).radiusX(radiusX).radiusZ(radiusZ);
        sender.sendMessage("format_center", translate("prefix"), centerX, centerZ);
        String shape;
        if (radiusX == radiusZ) {
            sender.sendMessage("format_radius", translate("prefix"), radiusX);
            shape = "square";
        } else {
            sender.sendMessage("format_radii", translate("prefix"), radiusX, radiusZ);
            shape = "rectangle";
        }
        chunky.getSelection().shape(shape);
        sender.sendMessage("format_shape", translate("prefix"), shape);
    }
}
