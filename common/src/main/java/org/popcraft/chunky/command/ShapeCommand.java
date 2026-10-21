package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.List;
import java.util.Optional;

import static org.popcraft.chunky.util.Translator.translate;

public class ShapeCommand implements ChunkyCommand {
    private final Chunky chunky;

    public ShapeCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final Optional<String> shapeName = arguments.next();
        if (shapeName.isEmpty()) {
            sender.sendMessage(TranslationKey.HELP_SHAPE);
            return;
        }
        final Optional<String> shape = Input.tryShape(shapeName.get());
        if (shape.isEmpty()) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_SHAPE_NOT_FOUND, shapeName.get(), String.join(", ", ShapeType.all().stream().sorted().toList()));
            return;
        }
        chunky.getSelection().shape(shape.get());
        sender.sendMessagePrefixed(TranslationKey.FORMAT_SHAPE, translate("shape_" + shape.get()));
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        if (arguments.size() == 1) {
            return ShapeType.all();
        }
        return List.of();
    }
}
