package org.popcraft.chunky;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Chunky extends JavaPlugin {
    private ConcurrentHashMap<World, GenTask> genTasks;
    private World world;
    private int x, z, radius;
    private boolean silent;
    private int quiet;

    private final static String FORMAT_START = "[Chunky] Task %s for %s at %d, %d with radius %d.";
    private final static String FORMAT_STARTED_ALREADY = "[Chunky] Task already started for %s!";
    private final static String FORMAT_STOP = "[Chunky] Task %s for %s...";
    private final static String FORMAT_WORLD = "[Chunky] World changed to %s.";
    private final static String FORMAT_RADIUS = "[Chunky] Radius changed to %d.";
    private final static String FORMAT_CENTER = "[Chunky] Center changed to %d, %d.";

    @Override
    public void onEnable() {
        this.genTasks = new ConcurrentHashMap<>();
        this.world = this.getServer().getWorlds().get(0);
        this.x = 0;
        this.z = 0;
        this.radius = 500;
        this.silent = false;
        this.quiet = 5;
    }

    @Override
    public void onDisable() {
        this.getServer().getConsoleSender().sendMessage(String.valueOf(genTasks.size()));
        stop(this.getServer().getConsoleSender(), new String[]{"stop"});
        this.getServer().getConsoleSender().sendMessage(String.valueOf(genTasks.size()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "start":
            case "queue":
                return start(sender, args);
            case "pause":
            case "stop":
                return stop(sender, args);
            case "continue":
                return cont(sender, args);
            case "world":
                return world(sender, args);
            case "center":
                return center(sender, args);
            case "radius":
                return radius(sender, args);
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "queue", "pause", "stop", "continue", "world", "center", "radius");
        }
        if (args.length == 2 && "world".equalsIgnoreCase(args[0])) {
            return Bukkit.getWorlds().stream().map(World::getName).map(String::toLowerCase).filter(w -> w.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean start(CommandSender sender, String[] args) {
        if (genTasks.containsKey(world)) {
            sender.sendMessage(String.format(FORMAT_STARTED_ALREADY, world.getName()));
            return true;
        }
        GenTask genTask = new GenTask(this, world, radius);
        genTasks.put(world, genTask);
        this.getServer().getScheduler().runTaskAsynchronously(this, genTask);
        String verb = "start".equalsIgnoreCase(args[0]) ? "started" : "queued";
        sender.sendMessage(String.format(FORMAT_START, verb, world.getName(), x, z, radius));
        return true;
    }

    private boolean stop(CommandSender sender, String[] args) {
        final String verb = "pause".equalsIgnoreCase(args[0]) ? "pausing" : "stopping";
        for (GenTask genTask : genTasks.values()) {
            genTask.cancel();
            sender.sendMessage(String.format(FORMAT_STOP, verb, genTask.getWorld().getName()));
        }
        genTasks.clear();
        this.getServer().getScheduler().cancelTasks(this);
        return true;
    }

    private boolean cont(CommandSender sender, String[] args) {
        return false;
    }

    private boolean world(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }
        Optional<World> newWorld = Input.tryWorld(args[1]);
        if (!newWorld.isPresent()) {
            return false;
        }
        this.world = newWorld.get();
        sender.sendMessage(String.format(FORMAT_WORLD, world.getName()));
        return true;
    }

    private boolean radius(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }
        Optional<Integer> newRadius = Input.tryInteger(args[1]);
        if (!newRadius.isPresent()) {
            return false;
        }
        this.radius = newRadius.get();
        sender.sendMessage(String.format(FORMAT_RADIUS, radius));
        return true;
    }

    private boolean center(CommandSender sender, String[] args) {
        Optional<Integer> newX = Optional.empty();
        if (args.length > 1) {
            newX = Input.tryInteger(args[1]);
        }
        Optional<Integer> newZ = Optional.empty();
        if (args.length > 2) {
            newZ = Input.tryInteger(args[2]);
        }
        if (!newX.isPresent() || !newZ.isPresent()) {
            return false;
        }
        this.x = newX.get();
        this.z = newZ.get();
        sender.sendMessage(String.format(FORMAT_CENTER, x, z));
        return true;
    }

    public ConcurrentHashMap<World, GenTask> getGenTasks() {
        return genTasks;
    }
}
