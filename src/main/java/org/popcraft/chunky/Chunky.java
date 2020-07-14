package org.popcraft.chunky;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Chunky extends JavaPlugin {
    private List<GenTask> genTasks;

    @Override
    public void onEnable() {
        this.genTasks = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        genTasks.forEach(GenTask::cancel);
        this.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final World world = sender instanceof Player ? ((Player) sender).getWorld() : this.getServer().getWorlds().get(0);
        final int radius = Integer.parseInt(args[0]);
        GenTask genTask = new GenTask(this, world, radius);
        this.getServer().getScheduler().runTaskAsynchronously(this, genTask);
        return true;
    }
}
