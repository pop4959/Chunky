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

public class CancelCommand extends ChunkyCommand {
    public CancelCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(Sender sender, String[] args) {
        final Map<World, GenerationTask> generationTasks = chunky.getGenerationTasks();
        if (generationTasks.isEmpty() && chunky.getConfig().loadTasks().isEmpty()) {
            sender.sendMessagePrefixed("format_cancel_no_tasks");
            return;
        }
        final Runnable cancelAction;
        if (args.length > 1) {
            final Optional<World> world = Input.tryWorld(chunky, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            if (!world.isPresent()) {
                sender.sendMessage("help_cancel");
                return;
            }
            cancelAction = () -> {
                sender.sendMessagePrefixed("format_cancel", world.get().getName());
                chunky.getConfig().cancelTask(world.get());
                if (chunky.getGenerationTasks().containsKey(world.get())) {
                    chunky.getGenerationTasks().remove(world.get()).stop(true);
                }
            };
        } else {
            cancelAction = () -> {
                sender.sendMessagePrefixed("format_cancel_all");
                chunky.getConfig().cancelTasks();
                chunky.getGenerationTasks().values().forEach(generationTask -> generationTask.stop(true));
                chunky.getGenerationTasks().clear();
                chunky.getScheduler().cancelTasks();
            };
        }
        chunky.setPendingAction(sender, cancelAction);
        sender.sendMessagePrefixed("format_cancel_confirm", "/chunky confirm");
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
