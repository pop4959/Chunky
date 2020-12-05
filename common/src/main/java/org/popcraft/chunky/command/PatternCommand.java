package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;

import java.util.Arrays;
import java.util.List;

import static org.popcraft.chunky.Chunky.translate;

public class PatternCommand extends ChunkyCommand {
    private static final List<String> PATTERNS = Arrays.asList("concentric", "loop", "spiral");

    public PatternCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("help_pattern");
            return;
        }
        String pattern = args[1].toLowerCase();
        if (!PATTERNS.contains(pattern)) {
            sender.sendMessage("help_pattern");
            return;
        }
        chunky.getSelection().pattern = pattern;
        sender.sendMessage("format_pattern", translate("prefix"), pattern);
    }

    @Override
    public List<String> tabSuggestions(Sender sender, String[] args) {
        return PATTERNS;
    }
}
