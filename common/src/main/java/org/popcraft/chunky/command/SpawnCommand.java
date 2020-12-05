package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Coordinate;

import static org.popcraft.chunky.Chunky.translate;

public class SpawnCommand extends ChunkyCommand {
    public SpawnCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        Selection selection = chunky.getSelection();
        Coordinate spawn = selection.world.getSpawnCoordinate();
        selection.centerX = spawn.getX();
        selection.centerZ = spawn.getZ();
        sender.sendMessage("format_center", translate("prefix"), selection.centerX, selection.centerZ);
    }
}
