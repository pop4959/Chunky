package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.List;
import java.util.Optional;

import static org.popcraft.chunky.util.Translator.translate;

public class RadiusCommand implements ChunkyCommand {
    private final Chunky chunky;

    public RadiusCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final Optional<String> newX = arguments.next();
        final Optional<Integer> signX = newX.flatMap(Input::trySign);
        final Optional<Double> newRadiusX = newX.map(x -> signX.isPresent() ? x.substring(1) : x).flatMap(Input::tryDoubleSuffixed);
        if (newRadiusX.isEmpty() || newRadiusX.get() < 0 || Input.isPastWorldLimit(newRadiusX.get())) {
            sender.sendMessage(TranslationKey.HELP_RADIUS);
            return;
        }
        final Selection current = chunky.getSelection().build();
        final double radiusX = signX.map(sign -> current.radiusX() + sign * newRadiusX.get()).orElseGet(newRadiusX::get);
        if (radiusX < 0 || Input.isPastWorldLimit(radiusX)) {
            sender.sendMessage(TranslationKey.HELP_RADIUS);
            return;
        }
        final Optional<String> newZ = arguments.next();
        final double radiusZ;
        if (newZ.isPresent()) {
            final Optional<Integer> signZ = newZ.flatMap(Input::trySign);
            final Optional<Double> newRadiusZ = newZ.map(z -> signZ.isPresent() ? z.substring(1) : z).flatMap(Input::tryDoubleSuffixed);
            if (newRadiusZ.isEmpty() || newRadiusZ.get() < 0 || Input.isPastWorldLimit(newRadiusZ.get())) {
                sender.sendMessage(TranslationKey.HELP_RADIUS);
                return;
            }
            radiusZ = signZ.map(sign -> current.radiusZ() + sign * newRadiusZ.get()).orElseGet(newRadiusZ::get);
            if (radiusZ < 0 || Input.isPastWorldLimit(radiusZ)) {
                sender.sendMessage(TranslationKey.HELP_RADIUS);
                return;
            }
        } else {
            radiusZ = radiusX;
        }
        chunky.getSelection().radiusX(radiusX).radiusZ(radiusZ);
        if (radiusX == radiusZ) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_RADIUS, Formatting.number(radiusX));
        } else {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_RADII, Formatting.number(radiusX), Formatting.number(radiusZ));
        }
        final String shape = switch (current.shape()) {
            case ShapeType.SQUARE, ShapeType.RECTANGLE -> radiusX == radiusZ ? ShapeType.SQUARE : ShapeType.RECTANGLE;
            case ShapeType.CIRCLE, ShapeType.ELLIPSE -> radiusX == radiusZ ? ShapeType.CIRCLE : ShapeType.ELLIPSE;
            default -> current.shape();
        };
        if (!shape.equals(current.shape())) {
            chunky.getSelection().shape(shape);
            sender.sendMessagePrefixed(TranslationKey.FORMAT_SHAPE, translate("shape_" + shape));
        }
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        return List.of();
    }
}
