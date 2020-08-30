package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;

import java.util.Arrays;
import java.util.List;

public class ShapeCommand extends ChunkyCommand {
    private static final List<String> SHAPES = Arrays.asList("circle", "diamond", "pentagon", "square", "star", "triangle");

    public ShapeCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chunky.message("help_shape"));
            return;
        }
        String shape = args[1].toLowerCase();
        if (!SHAPES.contains(shape)) {
            sender.sendMessage(chunky.message("help_shape"));
            return;
        }
        Selection selection = chunky.getSelection();
        selection.shape = shape;
        sender.sendMessage(chunky.message("format_shape", shape));
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return SHAPES;
    }
}
