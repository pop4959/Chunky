package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.platform.Sender;

import static org.popcraft.chunky.Chunky.translate;

public class ReloadCommand extends ChunkyCommand {
    public ReloadCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(Sender sender, String[] args) {
        if (chunky.getGenerationTasks().size() > 0) {
            sender.sendMessage("format_reload_tasks_running", translate("prefix"));
            return;
        }
        chunky.getPlatform().getServer().getConfig().reload();
        sender.sendMessage("format_reload", translate("prefix"));
    }
}
