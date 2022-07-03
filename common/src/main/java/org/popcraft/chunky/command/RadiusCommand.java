package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Optional;

public class RadiusCommand extends ChunkyCommand {
    public RadiusCommand(final Chunky chunky) {
        super(chunky);
    }

    public void execute(final Sender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TranslationKey.HELP_RADIUS);
            return;
        }
        final Optional<Integer> signX = Input.trySign(args[1]);
        final Optional<Double> newRadiusX = Input.tryDoubleSuffixed(signX.isPresent() ? args[1].substring(1) : args[1]);
        if (!newRadiusX.isPresent() || newRadiusX.get() < 0 || Input.isPastWorldLimit(newRadiusX.get())) {
            sender.sendMessage(TranslationKey.HELP_RADIUS);
            return;
        }
        final Selection current = chunky.getSelection().build();
        final double radiusX = signX.map(sign -> current.radiusX() + sign * newRadiusX.get()).orElseGet(newRadiusX::get);
        if (radiusX < 0 || Input.isPastWorldLimit(radiusX)) {
            sender.sendMessage(TranslationKey.HELP_RADIUS);
            return;
        }
        if (args.length > 2) {
            final Optional<Integer> signZ = Input.trySign(args[2]);
            final Optional<Double> newRadiusZ = Input.tryDoubleSuffixed(signZ.isPresent() ? args[2].substring(1) : args[2]);
            if (!newRadiusZ.isPresent() || newRadiusZ.get() < 0 || Input.isPastWorldLimit(newRadiusZ.get())) {
                sender.sendMessage(TranslationKey.HELP_RADIUS);
                return;
            }
            final double radiusZ = signZ.map(sign -> current.radiusZ() + sign * newRadiusZ.get()).orElseGet(newRadiusZ::get);
            if (radiusZ < 0 || Input.isPastWorldLimit(radiusZ)) {
                sender.sendMessage(TranslationKey.HELP_RADIUS);
                return;
            }
            chunky.getSelection().radiusX(radiusX).radiusZ(radiusZ);
            sender.sendMessagePrefixed(TranslationKey.FORMAT_RADII, Formatting.number(radiusX), Formatting.number(radiusZ));
        } else {
            chunky.getSelection().radius(radiusX);
            sender.sendMessagePrefixed(TranslationKey.FORMAT_RADIUS, Formatting.number(radiusX));
        }
    }
}
