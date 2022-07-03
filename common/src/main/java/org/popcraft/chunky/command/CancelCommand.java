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

public class CancelCommand extends ChunkyCommand {
    public CancelCommand(final Chunky chunky) {
        super(chunky);
    }

    public void execute(final Sender sender, final String[] args) {
        final Map<String, GenerationTask> generationTasks = chunky.getGenerationTasks();
        if (generationTasks.isEmpty() && chunky.getConfig().loadTasks().stream().allMatch(GenerationTask::isCancelled)) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_CANCEL_NO_TASKS);
            return;
        }
        final Runnable cancelAction;
        if (args.length > 1) {
            final Optional<World> world = Input.tryWorld(chunky, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            if (!world.isPresent()) {
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
    public List<String> tabSuggestions(final String[] args) {
        if (args.length == 2) {
            final List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        }
        return Collections.emptyList();
    }
}
