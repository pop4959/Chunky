package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ShapeCommand extends ChunkyCommand {
    public ShapeCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("help_shape");
            return;
        }
        Optional<String> inputShape = Input.tryShape(args[1]);
        if (!inputShape.isPresent()) {
            sender.sendMessage("help_shape");
            return;
        }
        String shape = inputShape.get();
        chunky.getSelection().shape(shape);
        sender.sendMessagePrefixed("format_shape", shape);
    }

    @Override
    public List<String> tabSuggestions(Sender sender, String[] args) {
        if (args.length == 2) {
            return Input.SHAPES;
        }
        return Collections.emptyList();
    }
}
