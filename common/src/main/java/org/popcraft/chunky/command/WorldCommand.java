package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WorldCommand extends ChunkyCommand {
    public WorldCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("help_world");
            return;
        }
        Optional<World> newWorld = Input.tryWorld(chunky, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        if (!newWorld.isPresent()) {
            sender.sendMessage("help_world");
            return;
        }
        chunky.getSelection().world(newWorld.get());
        sender.sendMessagePrefixed("format_world", newWorld.get().getName());
    }

    @Override
    public List<String> tabSuggestions(Sender sender, String[] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            chunky.getPlatform().getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        }
        return Collections.emptyList();
    }
}
