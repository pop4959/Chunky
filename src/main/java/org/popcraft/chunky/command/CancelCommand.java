package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;

public class CancelCommand extends ChunkyCommand {
    public CancelCommand(Chunky chunky) {
        super(chunky);
    }

    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(chunky.message("format_cancel", chunky.message("prefix")));
        chunky.getConfigStorage().cancelTasks();
        chunky.getTaskManager().stopAll(false, true, false);
        chunky.getServer().getScheduler().cancelTasks(chunky);
    }
}
