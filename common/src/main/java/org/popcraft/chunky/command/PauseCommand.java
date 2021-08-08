package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PauseCommand extends ChunkyCommand {
    public PauseCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        final Map<World, GenerationTask> generationTasks = chunky.getGenerationTasks();
        if (generationTasks.isEmpty()) {
            sender.sendMessagePrefixed("format_pause_no_tasks");
            return;
        }
        if (args.length > 1) {
            final Optional<World> world = Input.tryWorld(chunky, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            if (!world.isPresent() || !generationTasks.containsKey(world.get())) {
                sender.sendMessage("help_pause");
            } else {
                generationTasks.get(world.get()).stop(false);
                sender.sendMessagePrefixed("format_pause", world.get().getName());
            }
            return;
        }
        for (GenerationTask generationTask : chunky.getGenerationTasks().values()) {
            generationTask.stop(false);
            sender.sendMessagePrefixed("format_pause", generationTask.getSelection().world().getName());
        }
    }

    @Override
    public List<String> tabSuggestions(Sender sender, String[] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        }
        return Collections.emptyList();
    }
}
