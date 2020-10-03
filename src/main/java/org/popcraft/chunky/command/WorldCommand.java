package org.popcraft.chunky.command;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.Selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WorldCommand extends ChunkyCommand {
    public WorldCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chunky.message("help_world"));
            return;
        }
        Optional<World> newWorld = Input.tryWorld(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        if (!newWorld.isPresent()) {
            sender.sendMessage(chunky.message("help_world"));
            return;
        }
        Selection selection = chunky.getSelection();
        selection.world = newWorld.get();
        sender.sendMessage(chunky.message("format_world", chunky.message("prefix"), selection.world.getName()));
    }

    @Override
    public List<String> tabSuggestions(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();
        chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
        return suggestions;
    }
}
