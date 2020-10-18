package org.popcraft.chunky.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;

public class SpawnCommand extends ChunkyCommand {
    public SpawnCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Selection selection = chunky.getSelection();
        Location spawn = selection.world.getSpawnLocation();
        selection.centerX = spawn.getBlockX();
        selection.centerZ = spawn.getBlockZ();
        sender.sendMessage(chunky.message("format_center", chunky.message("prefix"), selection.centerX, selection.centerZ));
    }
}
