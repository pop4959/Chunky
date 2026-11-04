package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.util.List;
import java.util.Optional;

public class LimitCommand implements ChunkyCommand {
    private final Chunky chunky;

    public LimitCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        final Optional<Integer> newLimit = arguments.next().flatMap(Input::tryInteger);
        if (newLimit.isEmpty()) {
            sender.sendMessage(TranslationKey.HELP_LIMIT);
            return;
        }
        final int limit = Math.max(0, newLimit.get());
        chunky.getSelection().rateLimit(limit);
        final World world = chunky.getSelection().build().world();
        final GenerationTask generationTask = chunky.getGenerationTasks().get(world.getName());
        if (generationTask != null) {
            generationTask.getRateLimiter().setLimit(limit);
        } else {
            chunky.getTaskLoader().loadTask(world).filter(task -> !task.isCancelled()).ifPresent(task -> {
                task.getRateLimiter().setLimit(limit);
                chunky.getTaskLoader().saveTask(task);
            });
        }
        if (limit == 0) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_LIMIT_DISABLED, world.getName());
        } else {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_LIMIT, world.getName(), limit);
        }
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        return List.of();
    }
}
