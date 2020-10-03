package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;

import java.util.Arrays;
import java.util.List;

public class PatternCommand extends ChunkyCommand {
    private static final List<String> PATTERNS = Arrays.asList("concentric", "loop", "spiral");

    public PatternCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chunky.message("help_pattern"));
            return;
        }
        String pattern = args[1].toLowerCase();
        if (!PATTERNS.contains(pattern)) {
            sender.sendMessage(chunky.message("help_pattern"));
            return;
        }
        chunky.getSelection().pattern = pattern;
        sender.sendMessage(chunky.message("format_pattern", chunky.message("prefix"), pattern));
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return PATTERNS;
    }
}
