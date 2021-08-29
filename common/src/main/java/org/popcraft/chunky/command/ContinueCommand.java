package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

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
                sender.sendMessage(TranslationKey.HELP_CONTINUE);
                return;
            }
            loadTasks = chunky.getConfig().loadTask(world.get()).map(Collections::singletonList).orElse(Collections.emptyList());
        } else {
            loadTasks = chunky.getConfig().loadTasks();
        }
        if (loadTasks.isEmpty()) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_CONTINUE_NO_TASKS);
            return;
        }
        final Map<String, GenerationTask> generationTasks = chunky.getGenerationTasks();
        loadTasks.forEach(generationTask -> {
            World world = generationTask.getSelection().world();
            if (!generationTasks.containsKey(world.getName())) {
                generationTasks.put(world.getName(), generationTask);
                chunky.getScheduler().runTask(generationTask);
                sender.sendMessagePrefixed(TranslationKey.FORMAT_CONTINUE, world.getName());
            } else {
                sender.sendMessagePrefixed(TranslationKey.FORMAT_STARTED_ALREADY, world.getName());
            }
        });
    }

    @Override
    public List<String> tabSuggestions(String[] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        }
        return Collections.emptyList();
    }
}
