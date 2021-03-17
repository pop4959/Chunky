package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;

import static org.popcraft.chunky.Chunky.translate;

public class SpawnCommand extends ChunkyCommand {
    public SpawnCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        chunky.getSelection().spawn();
        Selection current = chunky.getSelection().build();
        sender.sendMessage("format_center", translate("prefix"), current.centerX(), current.centerZ());
    }
}
