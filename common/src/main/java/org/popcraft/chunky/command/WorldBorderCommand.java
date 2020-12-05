package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.integration.Integration;
import org.popcraft.chunky.integration.WorldBorderIntegration;
import org.popcraft.chunky.platform.Border;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Coordinate;

import java.util.Map;

import static org.popcraft.chunky.Chunky.translate;

public class WorldBorderCommand extends ChunkyCommand {
    public WorldBorderCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        Selection selection = chunky.getSelection();
        Map<String, Integration> integrations = chunky.getPlatform().getServer().getIntegrations();
        if (integrations.containsKey("border")) {
            WorldBorderIntegration worldborder = (WorldBorderIntegration) integrations.get("border");
            String worldName = selection.world.getName();
            if (worldborder.hasBorder(worldName)) {
                Border border = worldborder.getBorder(worldName);
                Coordinate center = border.getCenter();
                selection.centerX = center.getX();
                selection.centerZ = center.getZ();
                selection.radiusX = border.getRadiusX();
                selection.radiusZ = border.getRadiusZ();
                selection.shape = border.getShape();
                sender.sendMessage("format_center", translate("prefix"), selection.centerX, selection.centerZ);
                if (selection.radiusX == selection.radiusZ) {
                    sender.sendMessage("format_radius", translate("prefix"), selection.radiusX);
                } else {
                    sender.sendMessage("format_radii", translate("prefix"), selection.radiusX, selection.radiusZ);
                }
                sender.sendMessage("format_shape", translate("prefix"), selection.shape);
                return;
            }
        }
        // Default to the vanilla world border
        Border border = selection.world.getWorldBorder();
        Coordinate center = border.getCenter();
        selection.centerX = center.getX();
        selection.centerZ = center.getZ();
        selection.radiusX = border.getRadiusX();
        selection.radiusZ = border.getRadiusZ();
        sender.sendMessage("format_center", translate("prefix"), selection.centerX, selection.centerZ);
        sender.sendMessage("format_radius", translate("prefix"), selection.radiusX);
    }
}
