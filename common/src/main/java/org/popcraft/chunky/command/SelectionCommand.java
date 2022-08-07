package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.TranslationKey;

import java.util.List;

import static org.popcraft.chunky.util.Translator.translate;

public class SelectionCommand implements ChunkyCommand {
    private final Chunky chunky;

    public SelectionCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final Selection current = chunky.getSelection().build();
        sender.sendMessagePrefixed(TranslationKey.FORMAT_SELECTION);
        sender.sendMessage(TranslationKey.FORMAT_SELECTION_WORLD, current.world().getName());
        sender.sendMessage(TranslationKey.FORMAT_SELECTION_SHAPE, translate("shape_" + current.shape()));
        sender.sendMessage(TranslationKey.FORMAT_SELECTION_CENTER, Formatting.number(current.centerX()), Formatting.number(current.centerZ()));
        final double radiusX = current.radiusX();
        final double radiusZ = current.radiusZ();
        if (radiusX == radiusZ) {
            sender.sendMessage(TranslationKey.FORMAT_SELECTION_RADIUS, Formatting.number(radiusX));
        } else {
            sender.sendMessage(TranslationKey.FORMAT_SELECTION_RADII, Formatting.number(radiusX), Formatting.number(radiusZ));
        }
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        return List.of();
    }
}
