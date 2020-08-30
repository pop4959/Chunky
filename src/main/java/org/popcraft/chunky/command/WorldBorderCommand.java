package org.popcraft.chunky.command;

import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;

public class WorldBorderCommand extends ChunkyCommand {
    public WorldBorderCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        Selection selection = chunky.getSelection();
        WorldBorder border = selection.world.getWorldBorder();
        Location center = border.getCenter();
        selection.x = center.getBlockX();
        selection.z = center.getBlockZ();
        selection.radius = (int) border.getSize() / 2;
        sender.sendMessage(chunky.message("format_center", selection.x, selection.z));
        sender.sendMessage(chunky.message("format_radius", selection.radius));
    }
}
