package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Player;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.ArrayList;
import java.util.List;

public class WorldCommand implements ChunkyCommand {
    private final Chunky chunky;

    public WorldCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final World world;
        if (arguments.size() == 0 && sender instanceof final Player player) {
            world = player.getWorld();
        } else {
            world = Input.tryWorld(chunky, arguments.joined()).orElse(null);
        }
        if (world == null) {
            sender.sendMessage(TranslationKey.HELP_WORLD);
            return;
        }
        chunky.getSelection().world(world);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_WORLD, world.getName());
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        if (arguments.size() == 1) {
            final List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        }
        return List.of();
    }
}
