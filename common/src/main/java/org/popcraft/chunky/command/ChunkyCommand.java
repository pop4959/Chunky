package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;

import java.util.Collections;
import java.util.List;

public abstract class ChunkyCommand {
    protected final Chunky chunky;

    protected ChunkyCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    public abstract void execute(Sender sender, String[] args);

    @SuppressWarnings("unused")
    public List<String> tabSuggestions(final String[] args) {
        return Collections.emptyList();
    }
}
