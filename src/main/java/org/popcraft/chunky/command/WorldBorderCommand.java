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
                selection.x = Location.locToBlock(borderData.getX());
                selection.z = Location.locToBlock(borderData.getZ());
                selection.radius = borderData.getRadiusX();
                selection.zRadius = borderData.getRadiusZ();
                boolean round = borderData.getShape() != null && borderData.getShape();
                sender.sendMessage(chunky.message("format_center", selection.x, selection.z));
                if (selection.radius == selection.zRadius) {
                    selection.shape = round ? "circle" : "square";
                    sender.sendMessage(chunky.message("format_radius", selection.radius));
                } else {
                    selection.shape = round ? "oval" : "rectangle";
                    sender.sendMessage(chunky.message("format_radii", selection.radius, selection.zRadius));
                }
                sender.sendMessage(chunky.message("format_shape", selection.shape));
                return;
            }
        }
        // Default to the vanilla world border
        WorldBorder border = selection.world.getWorldBorder();
        Location center = border.getCenter();
        selection.x = center.getBlockX();
        selection.z = center.getBlockZ();
        selection.radius = (int) border.getSize() / 2;
        sender.sendMessage(chunky.message("format_center", selection.x, selection.z));
        sender.sendMessage(chunky.message("format_radius", selection.radius));
    }
}
