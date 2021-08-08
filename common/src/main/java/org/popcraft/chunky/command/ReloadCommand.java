package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Sender;

public class ReloadCommand extends ChunkyCommand {
    public ReloadCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        if (!chunky.getGenerationTasks().isEmpty()) {
            sender.sendMessagePrefixed("format_reload_tasks_running");
            return;
        }
        Config config = chunky.getServer().getConfig();
        config.reload();
        chunky.setLanguage(config.getLanguage());
        sender.sendMessagePrefixed("format_reload");
    }
}
