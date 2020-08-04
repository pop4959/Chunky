package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitWorker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Chunky extends JavaPlugin {
    private ConfigStorage configStorage;
    private ConcurrentHashMap<World, GenTask> genTasks;
    private World world;
    private int x, z, radius;
    private boolean queue;
    private boolean silent;
    private int quiet;
    private Metrics metrics;

    private static final String HELP_START = "§2chunky start§r - Start a new chunk generation task";
    private static final String HELP_PAUSE = "§2chunky pause§r - Pause current tasks and save progress";
    private static final String HELP_CONTINUE = "§2chunky continue§r - Continue current or saved tasks";
    private static final String HELP_CANCEL = "§2chunky cancel§r - Stop and delete current or saved tasks";
    private static final String HELP_WORLD = "§2chunky world <world>§r - Set the world target";
    private static final String HELP_CENTER = "§2chunky center <x> <z>§r - Set the center block location";
    private static final String HELP_RADIUS = "§2chunky radius <radius>§r - Set the radius";
    private static final String HELP_SILENT = "§2chunky silent§r - Toggle displaying update messages";
    private static final String HELP_QUIET = "§2chunky quiet <interval>§r - Set the quiet interval";
    private static final String HELP_MENU = String.format("§aChunky Commands§r\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s", HELP_START, HELP_PAUSE, HELP_CONTINUE, HELP_CANCEL, HELP_WORLD, HELP_CENTER, HELP_RADIUS, HELP_SILENT, HELP_QUIET);
    private static final String FORMAT_START = "[Chunky] Task started for %s at %d, %d with radius %d.";
    private static final String FORMAT_STARTED_ALREADY = "[Chunky] Task already started for %s!";
    private static final String FORMAT_PAUSE = "[Chunky] Task paused for %s.";
    private static final String FORMAT_CONTINUE = "[Chunky] Task continuing for %s.";
    private static final String FORMAT_CANCEL = "[Chunky] Cancelling all tasks.";
    private static final String FORMAT_WORLD = "[Chunky] World changed to %s.";
    private static final String FORMAT_CENTER = "[Chunky] Center changed to %d, %d.";
    private static final String FORMAT_RADIUS = "[Chunky] Radius changed to %d.";
    private static final String FORMAT_SILENT = "[Chunky] Silent mode %s.";
    private static final String FORMAT_QUIET = "[Chunky] Quiet interval set to %d seconds.";
    private static final String ERROR_SPIGOT_1_13 = "This plugin is not compatible with Spigot 1.13! Please use Paper instead.";
    private static final String ERROR_BELOW_1_13 = "This plugin is not compatible with Minecraft versions below 1.13.";

    @Override
    public void onEnable() {
        this.configStorage = new ConfigStorage(this);
        this.genTasks = new ConcurrentHashMap<>();
        this.world = this.getServer().getWorlds().get(0);
        this.x = 0;
        this.z = 0;
        this.radius = 500;
        this.silent = false;
        this.quiet = 1;
        this.metrics = new Metrics(this, 8211);
        if (BukkitVersion.v1_13_2.isEqualTo(BukkitVersion.getCurrent()) && !PaperLib.isPaper()) {
            this.getLogger().severe(ERROR_SPIGOT_1_13);
            this.getServer().getPluginManager().disablePlugin(this);
        } else if (BukkitVersion.v1_13_2.isHigherThan(BukkitVersion.getCurrent())) {
            this.getLogger().severe(ERROR_BELOW_1_13);
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        pause(this.getServer().getConsoleSender());
        this.getServer().getScheduler().getActiveWorkers().stream()
                .filter(w -> w.getOwner() == this)
                .map(BukkitWorker::getThread)
                .forEach(Thread::interrupt);
        this.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(HELP_MENU);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "start":
                start(sender);
                break;
            case "pause":
                pause(sender);
                break;
            case "continue":
                cont(sender);
                break;
            case "cancel":
                cancel(sender);
                break;
            case "world":
                world(sender, args);
                break;
            case "worldborder":
                worldBorder(sender);
                break;
            case "center":
                center(sender, args);
                break;
            case "radius":
                radius(sender, args);
                break;
            case "silent":
                silent(sender);
                break;
            case "quiet":
                quiet(sender, args);
                break;
            default:
                sender.sendMessage(HELP_MENU);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "pause", "continue", "world", "worldborder", "center", "radius", "silent", "quiet");
        }
        if (args.length == 2 && "world".equalsIgnoreCase(args[0])) {
            return Bukkit.getWorlds().stream().map(World::getName).map(String::toLowerCase).filter(w -> w.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void start(CommandSender sender) {
        if (genTasks.containsKey(world)) {
            sender.sendMessage(String.format(FORMAT_STARTED_ALREADY, world.getName()));
            return;
        }
        GenTask genTask = new GenTask(this, world, radius, x, z);
        genTasks.put(world, genTask);
        this.getServer().getScheduler().runTaskAsynchronously(this, genTask);
        sender.sendMessage(String.format(FORMAT_START, world.getName(), x, z, radius));
    }

    private void pause(CommandSender sender) {
        for (GenTask genTask : genTasks.values()) {
            genTask.stop(false);
            sender.sendMessage(String.format(FORMAT_PAUSE, genTask.getWorld().getName()));
        }
    }

    private void cont(CommandSender sender) {
        configStorage.loadTasks().forEach(genTask -> {
            if (!genTasks.containsKey(genTask.getWorld())) {
                genTasks.put(genTask.getWorld(), genTask);
                this.getServer().getScheduler().runTaskAsynchronously(this, genTask);
                sender.sendMessage(String.format(FORMAT_CONTINUE, genTask.getWorld().getName()));
            } else {
                sender.sendMessage(String.format(FORMAT_STARTED_ALREADY, genTask.getWorld().getName()));
            }
        });
    }

    private void cancel(CommandSender sender) {
        sender.sendMessage(FORMAT_CANCEL);
        configStorage.cancelTasks();
        genTasks.values().forEach(genTask -> genTask.stop(true));
        genTasks.clear();
        this.getServer().getScheduler().cancelTasks(this);
    }

    private void world(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(HELP_WORLD);
            return;
        }
        Optional<World> newWorld = Input.tryWorld(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        if (!newWorld.isPresent()) {
            sender.sendMessage(HELP_WORLD);
            return;
        }
        this.world = newWorld.get();
        sender.sendMessage(String.format(FORMAT_WORLD, world.getName()));
    }

    private void worldBorder(CommandSender sender) {
        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        this.x = center.getBlockX();
        this.z = center.getBlockZ();
        this.radius = (int) border.getSize() / 2;
        sender.sendMessage(String.format(FORMAT_CENTER, x, z));
        sender.sendMessage(String.format(FORMAT_RADIUS, radius));
    }

    private void center(CommandSender sender, String[] args) {
        Optional<Integer> newX = Optional.empty();
        if (args.length > 1) {
            newX = Input.tryInteger(args[1]);
        }
        Optional<Integer> newZ = Optional.empty();
        if (args.length > 2) {
            newZ = Input.tryInteger(args[2]);
        }
        if (!newX.isPresent() || !newZ.isPresent()) {
            sender.sendMessage(HELP_CENTER);
            return;
        }
        this.x = newX.get();
        this.z = newZ.get();
        sender.sendMessage(String.format(FORMAT_CENTER, x, z));
    }

    private void radius(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(HELP_RADIUS);
            return;
        }
        Optional<Integer> newRadius = Input.tryInteger(args[1]);
        if (!newRadius.isPresent()) {
            sender.sendMessage(HELP_RADIUS);
            return;
        }
        this.radius = newRadius.get();
        sender.sendMessage(String.format(FORMAT_RADIUS, radius));
    }

    private void silent(CommandSender sender) {
        this.silent = !silent;
        sender.sendMessage(String.format(FORMAT_SILENT, silent ? "enabled" : "disabled"));
    }

    private void quiet(CommandSender sender, String[] args) {
        Optional<Integer> newQuiet = Optional.empty();
        if (args.length > 1) {
            newQuiet = Input.tryInteger(args[1]);
        }
        if (!newQuiet.isPresent()) {
            sender.sendMessage(HELP_QUIET);
            return;
        }
        this.quiet = newQuiet.get();
        sender.sendMessage(String.format(FORMAT_QUIET, quiet));
    }

    public ConfigStorage getConfigStorage() {
        return configStorage;
    }

    public ConcurrentHashMap<World, GenTask> getGenTasks() {
        return genTasks;
    }

    public boolean isSilent() {
        return silent;
    }

    public int getQuiet() {
        return quiet;
    }
}
