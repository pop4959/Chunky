package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.popcraft.chunky.util.Translator.translate;

public class ShapeCommand extends ChunkyCommand {
    public ShapeCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TranslationKey.HELP_SHAPE);
            return;
        }
        Optional<String> inputShape = Input.tryShape(args[1]);
        if (!inputShape.isPresent()) {
            sender.sendMessage(TranslationKey.HELP_SHAPE);
            return;
        }
        String shape = inputShape.get();
        chunky.getSelection().shape(shape);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_SHAPE, translate("shape_" + shape));
    }

    @Override
    public List<String> tabSuggestions(String[] args) {
        if (args.length == 2) {
            return ShapeType.ALL;
        }
        return Collections.emptyList();
    }
}
