package org.popcraft.chunky.command;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
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
        if (chunky.getServer().getPluginManager().getPlugin("WorldBorder") != null) {
            // WorldBorder is installed, use its border
            BorderData borderData = Config.Border(selection.world.getName());
            if (borderData != null) {
                selection.centerX = Location.locToBlock(borderData.getX());
                selection.centerZ = Location.locToBlock(borderData.getZ());
                selection.radiusX = borderData.getRadiusX();
                selection.radiusZ = borderData.getRadiusZ();
                boolean round = borderData.getShape() != null && borderData.getShape();
                sender.sendMessage(chunky.message("format_center", selection.centerX, selection.centerZ));
                if (selection.radiusX == selection.radiusZ) {
                    selection.shape = round ? "circle" : "square";
                    sender.sendMessage(chunky.message("format_radius", selection.radiusX));
                } else {
                    selection.shape = round ? "oval" : "rectangle";
                    sender.sendMessage(chunky.message("format_radii", selection.radiusX, selection.radiusZ));
                }
                sender.sendMessage(chunky.message("format_shape", selection.shape));
                return;
            }
        }
        // Default to the vanilla world border
        WorldBorder border = selection.world.getWorldBorder();
        Location center = border.getCenter();
        selection.centerX = center.getBlockX();
        selection.centerZ = center.getBlockZ();
        selection.radiusX = selection.radiusZ = (int) border.getSize() / 2;
        sender.sendMessage(chunky.message("format_center", selection.centerX, selection.centerZ));
        sender.sendMessage(chunky.message("format_radius", selection.radiusX));
    }
}
