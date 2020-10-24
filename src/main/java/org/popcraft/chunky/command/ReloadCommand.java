package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;

public class ReloadCommand extends ChunkyCommand {
    public ReloadCommand(Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (chunky.getGenerationTasks().size() > 0) {
            sender.sendMessage(chunky.message("format_reload_tasks_running", chunky.message("prefix")));
            return;
        }
        chunky.reloadConfig();
        sender.sendMessage(chunky.message("format_reload", chunky.message("prefix")));
    }
}
