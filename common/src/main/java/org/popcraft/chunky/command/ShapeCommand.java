package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.popcraft.chunky.Chunky.translate;

public class ShapeCommand extends ChunkyCommand {
    public ShapeCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("help_shape");
            return;
        }
        Optional<String> shape = Input.tryShape(args[1]);
        if (!shape.isPresent()) {
            sender.sendMessage("help_shape");
            return;
        }
        Selection selection = chunky.getSelection();
        selection.shape = shape.get();
        sender.sendMessage("format_shape", translate("prefix"), selection.shape);
    }

    @Override
    public List<String> tabSuggestions(Sender sender, String[] args) {
        if (args.length == 2) {
            return Input.SHAPES;
        }
        return Collections.emptyList();
    }
}
