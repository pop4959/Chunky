package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Coordinate;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.Input;

import java.util.Optional;

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
        final double centerX;
        final double centerZ;
        if (!newX.isPresent() && !newZ.isPresent()) {
            Coordinate coordinate = sender.getCoordinate();
            centerX = coordinate.getX();
            centerZ = coordinate.getZ();
        } else if (newX.isPresent() && newZ.isPresent()) {
            centerX = newX.get();
            centerZ = newZ.get();
        } else {
            sender.sendMessage("help_center");
            return;
        }
        if (Input.isPastWorldLimit(centerX) || Input.isPastWorldLimit(centerZ)) {
            sender.sendMessage("help_center");
            return;
        }
        chunky.getSelection().center(centerX, centerZ);
        sender.sendMessagePrefixed("format_center", Formatting.number(centerX), Formatting.number(centerZ));
    }
}
