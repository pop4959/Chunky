package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;

import java.util.Collections;
import java.util.List;

public abstract class ChunkyCommand {
    protected final Chunky chunky;

    public ChunkyCommand(Chunky chunky) {
        this.chunky = chunky;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
