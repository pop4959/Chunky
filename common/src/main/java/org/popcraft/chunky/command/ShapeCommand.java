package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.popcraft.chunky.Chunky.translate;

public class ShapeCommand extends ChunkyCommand {
    private static final List<String> SHAPES = Arrays.asList("circle", "diamond", "oval", "pentagon", "rectangle", "square", "star", "triangle");

    public ShapeCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("help_shape");
            return;
        }
        String shape = args[1].toLowerCase();
        if (!SHAPES.contains(shape)) {
            sender.sendMessage("help_shape");
            return;
        }
        Selection selection = chunky.getSelection();
        selection.shape = shape;
        sender.sendMessage("format_shape", translate("prefix"), shape);
    }

    @Override
    public List<String> tabSuggestions(Sender sender, String[] args) {
        if (args.length == 2) {
            return Input.SHAPES;
        }
        return Collections.emptyList();
    }
}
