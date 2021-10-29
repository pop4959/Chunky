package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.integration.BorderIntegration;
import org.popcraft.chunky.integration.Integration;
import org.popcraft.chunky.platform.Border;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.platform.util.Vector2;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.TranslationKey;

import java.util.Map;

public class WorldBorderCommand extends ChunkyCommand {
    public WorldBorderCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        Selection previous = chunky.getSelection().build();
        if (!setBorderViaIntegration(previous.world())) {
            chunky.getSelection().worldborder();
        }
        Selection current = chunky.getSelection().build();
        sender.sendMessagePrefixed(TranslationKey.FORMAT_CENTER, Formatting.number(current.centerX()), Formatting.number(current.centerZ()));
        if (current.radiusX() == current.radiusZ()) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_RADIUS, Formatting.number(current.radiusX()));
        } else {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_RADII, Formatting.number(current.radiusX()), Formatting.number(current.radiusZ()));
        }
        if (!previous.shape().equals(current.shape())) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_SHAPE, current.shape());
        }
    }

    boolean setBorderViaIntegration(World world) {
        Map<String, Integration> integrations = chunky.getServer().getIntegrations();
        if (integrations.containsKey("border")) {
            BorderIntegration worldborder = (BorderIntegration) integrations.get("border");
            String worldName = world.getName();
            if (worldborder.hasBorder(worldName)) {
                Border border = worldborder.getBorder(worldName);
                Vector2 center = border.getCenter();
                chunky.getSelection().center(center.getX(), center.getZ())
                        .radiusX(border.getRadiusX()).radiusZ(border.getRadiusZ())
                        .shape(border.getShape());
                return true;
            }
        }
        return false;
    }
}
