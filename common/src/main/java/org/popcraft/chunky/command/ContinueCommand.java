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

public class ContinueCommand extends ChunkyCommand {
    public ContinueCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        final List<GenerationTask> loadTasks;
        if (args.length > 1) {
            final Optional<World> world = Input.tryWorld(chunky, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            if (!world.isPresent()) {
                sender.sendMessage("help_continue");
                return;
            }
            loadTasks = chunky.getConfig().loadTask(world.get()).map(Collections::singletonList).orElse(Collections.emptyList());
        } else {
            loadTasks = chunky.getConfig().loadTasks();
        }
        if (loadTasks.isEmpty()) {
            sender.sendMessagePrefixed("format_continue_no_tasks");
            return;
        }
        final Map<World, GenerationTask> generationTasks = chunky.getGenerationTasks();
        loadTasks.forEach(generationTask -> {
            World world = generationTask.getSelection().world();
            if (!generationTasks.containsKey(world)) {
                generationTasks.put(world, generationTask);
                chunky.getServer().getScheduler().runTaskAsync(generationTask);
                sender.sendMessagePrefixed("format_continue", world.getName());
            } else {
                sender.sendMessagePrefixed("format_started_already", world.getName());
            }
        });
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
