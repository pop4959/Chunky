package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;

public class CancelCommand extends ChunkyCommand {
    public CancelCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        if (chunky.getTaskManager().getTasks().size() == 0 && chunky.getConfigStorage().loadTasks().size() == 0) {
            sender.sendMessage(chunky.message("format_cancel_no_tasks", chunky.message("prefix")));
            return;
        }
        sender.sendMessage(chunky.message("format_cancel", chunky.message("prefix")));
        chunky.getConfigStorage().cancelTasks();
        chunky.getTaskManager().stopAll(false, true, false);
        chunky.getServer().getScheduler().cancelTasks(chunky);
    }
}
