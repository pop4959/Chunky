package org.popcraft.chunky.command;

import org.popcraft.chunky.platform.Sender;

import java.util.List;

public interface ChunkyCommand {
    void execute(Sender sender, CommandArguments arguments);

    List<String> suggestions(final CommandArguments arguments);
}
