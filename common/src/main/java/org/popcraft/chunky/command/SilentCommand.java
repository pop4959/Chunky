package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;

import static org.popcraft.chunky.Chunky.translate;

public class SilentCommand extends ChunkyCommand {
    public SilentCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        Selection selection = chunky.getSelection();
        selection.silent = !selection.silent;
        String status = translate(selection.silent ? "enabled" : "disabled");
        sender.sendMessage("format_silent", translate("prefix"), status);
    }
}
