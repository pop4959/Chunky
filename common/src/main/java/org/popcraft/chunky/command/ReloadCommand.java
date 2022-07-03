package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.TranslationKey;

public class ReloadCommand extends ChunkyCommand {
    public ReloadCommand(final Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(final Sender sender, final String[] args) {
        if (!chunky.getGenerationTasks().isEmpty()) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_RELOAD_TASKS_RUNNING);
            return;
        }
        final Config config = chunky.getServer().getConfig();
        config.reload();
        chunky.setLanguage(config.getLanguage());
        sender.sendMessagePrefixed(TranslationKey.FORMAT_RELOAD);
    }
}
