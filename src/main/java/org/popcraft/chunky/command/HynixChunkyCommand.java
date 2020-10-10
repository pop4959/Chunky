package org.popcraft.chunky.command;

import org.bukkit.command.CommandSender;
import org.popcraft.chunky.Chunky;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@CommandPermission("chunky-hynix.command")
@CommandAlias("chunky-hynix")
@RequiredArgsConstructor
public class HynixChunkyCommand extends BaseCommand {
    private @NonNull Chunky instance;

    @Default
    public void onDefaultCommand(CommandIssuer sender, CommandHelp help) {
        help.showHelp(sender);
    }

    @Subcommand("status|progress")
    public void onGetTasksStatus(CommandSender sender) {
        var tasks = instance.getTaskManager().getTasks();
        sender.sendMessage(tasks.isEmpty() ? "No tasks currently running" : "Tasks running: ");
        tasks.forEach(task -> sender.sendMessage(String.format(" - %s is at %.2f%s", task.getWorld().getName(), task.getPercentDone(), "%")));
    }

}
