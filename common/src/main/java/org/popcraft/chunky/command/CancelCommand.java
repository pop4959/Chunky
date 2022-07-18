package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CancelCommand implements ChunkyCommand {
    private final Chunky chunky;

    public CancelCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final Map<String, GenerationTask> generationTasks = chunky.getGenerationTasks();
        if (generationTasks.isEmpty() && chunky.getConfig().loadTasks().stream().allMatch(GenerationTask::isCancelled)) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_CANCEL_NO_TASKS);
            return;
        }
        final Runnable cancelAction;
        if (arguments.size() > 0) {
            final Optional<World> world = Input.tryWorld(chunky, arguments.joined());
            if (world.isEmpty()) {
                sender.sendMessage(TranslationKey.HELP_CANCEL);
                return;
            }
            cancelAction = () -> {
                sender.sendMessagePrefixed(TranslationKey.FORMAT_CANCEL, world.get().getName());
                chunky.getConfig().cancelTask(world.get());
                if (chunky.getGenerationTasks().containsKey(world.get().getName())) {
                    chunky.getGenerationTasks().remove(world.get().getName()).stop(true);
                }
            };
        } else {
            cancelAction = () -> {
                sender.sendMessagePrefixed(TranslationKey.FORMAT_CANCEL_ALL);
                chunky.getConfig().cancelTasks();
                chunky.getGenerationTasks().values().forEach(generationTask -> generationTask.stop(true));
                chunky.getGenerationTasks().clear();
                chunky.getScheduler().cancelTasks();
            };
        }
        chunky.setPendingAction(sender, cancelAction);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_CANCEL_CONFIRM, "/chunky confirm");
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
