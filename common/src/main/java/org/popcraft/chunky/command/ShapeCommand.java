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
        final Optional<String> inputShape = arguments.next().flatMap(Input::tryShape);
        if (inputShape.isEmpty()) {
            sender.sendMessage(TranslationKey.HELP_SHAPE);
            return;
        }
        final String shape = inputShape.get();
        chunky.getSelection().shape(shape);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_SHAPE, translate("shape_" + shape));
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        if (arguments.size() == 1) {
            return ShapeType.ALL;
        }
        return List.of();
    }
}
