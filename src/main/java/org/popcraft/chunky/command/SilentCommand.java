package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;

public class SilentCommand extends ChunkyCommand {
    public SilentCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        Selection selection = chunky.getSelection();
        selection.silent = !selection.silent;
        String status = selection.silent ? chunky.message("enabled") : chunky.message("disabled");
        sender.sendMessage(chunky.message("format_silent", status));
    }
}
