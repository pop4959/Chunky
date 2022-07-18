package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldCommand implements ChunkyCommand {
    private final Chunky chunky;

    public WorldCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final Optional<World> newWorld = Input.tryWorld(chunky, arguments.joined());
        if (newWorld.isEmpty()) {
            sender.sendMessage(TranslationKey.HELP_WORLD);
            return;
        }
        chunky.getSelection().world(newWorld.get());
        sender.sendMessagePrefixed(TranslationKey.FORMAT_WORLD, newWorld.get().getName());
    }

    @Override
    public List<String> tabSuggestions(final CommandArguments arguments) {
        if (arguments.size() == 1) {
            final List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        }
        return List.of();
    }
}
