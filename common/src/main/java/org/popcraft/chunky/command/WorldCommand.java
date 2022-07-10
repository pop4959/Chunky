package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WorldCommand extends ChunkyCommand {
    public WorldCommand(final Chunky chunky) {
        super(chunky);
    }

    public void execute(final Sender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TranslationKey.HELP_WORLD);
            return;
        }
        final Optional<World> newWorld = Input.tryWorld(chunky, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        if (newWorld.isEmpty()) {
            sender.sendMessage(TranslationKey.HELP_WORLD);
            return;
        }
        chunky.getSelection().world(newWorld.get());
        sender.sendMessagePrefixed(TranslationKey.FORMAT_WORLD, newWorld.get().getName());
    }

    @Override
    public List<String> tabSuggestions(final String[] args) {
        if (args.length == 2) {
            final List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        }
        return Collections.emptyList();
    }
}
