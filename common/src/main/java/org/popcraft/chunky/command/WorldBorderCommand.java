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

import java.util.List;
import java.util.Map;

public class WorldBorderCommand implements ChunkyCommand {
    private final Chunky chunky;

    public WorldBorderCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final Selection previous = chunky.getSelection().build();
        if (!setBorderViaIntegration(previous.world())) {
            chunky.getSelection().worldborder();
        }
        final Selection current = chunky.getSelection().build();
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

    @Override
    public List<String> tabSuggestions(final CommandArguments arguments) {
        return List.of();
    }

    boolean setBorderViaIntegration(final World world) {
        final Map<String, Integration> integrations = chunky.getServer().getIntegrations();
        if (integrations.containsKey("border")) {
            final BorderIntegration worldborder = (BorderIntegration) integrations.get("border");
            final String worldName = world.getName();
            if (worldborder.hasBorder(worldName)) {
                final Border border = worldborder.getBorder(worldName);
                final Vector2 center = border.getCenter();
                chunky.getSelection().center(center.getX(), center.getZ())
                        .radiusX(border.getRadiusX()).radiusZ(border.getRadiusZ())
                        .shape(border.getShape());
                return true;
            }
        }
        return false;
    }
}
