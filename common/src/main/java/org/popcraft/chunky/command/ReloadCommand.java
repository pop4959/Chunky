package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.TranslationKey;

import java.util.List;

public class ReloadCommand implements ChunkyCommand {
    private final Chunky chunky;

    public ReloadCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        if (!chunky.getGenerationTasks().isEmpty()) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_RELOAD_TASKS_RUNNING);
            return;
        }
        final Config config = chunky.getServer().getConfig();
        config.reload();
        chunky.setLanguage(config.getLanguage());
        sender.sendMessagePrefixed(TranslationKey.FORMAT_RELOAD);
    }

    @Override
    public List<String> tabSuggestions(final CommandArguments arguments) {
        return List.of();
    }
}
