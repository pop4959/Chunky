package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.World;
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

    private final static String HELP_START = "§2chunky start§r - Start a new chunk generation task";
    private final static String HELP_PAUSE = "§2chunky pause§r - Pause current tasks and save progress";
    private final static String HELP_CONTINUE = "§2chunky continue§r - Continue current or saved tasks";
    private final static String HELP_CANCEL = "§2chunky cancel§r - Stop and delete current or saved tasks";
    private final static String HELP_WORLD = "§2chunky world <world>§r - Set the world target";
    private final static String HELP_CENTER = "§2chunky center <x> <z>§r - Set the center block location";
    private final static String HELP_RADIUS = "§2chunky radius <radius>§r - Set the radius";
    private final static String HELP_SILENT = "§2chunky silent§r - Toggle displaying update messages";
    private final static String HELP_QUIET = "§2chunky quiet <interval>§r - Set the quiet interval";
    private final static String HELP_MENU = String.format("§aChunky Commands§r\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s", HELP_START, HELP_PAUSE, HELP_CONTINUE, HELP_CANCEL, HELP_WORLD, HELP_CENTER, HELP_RADIUS, HELP_SILENT, HELP_QUIET);
    private final static String FORMAT_START = "[Chunky] Task started for %s at %d, %d with radius %d.";
    private final static String FORMAT_STARTED_ALREADY = "[Chunky] Task already started for %s!";
    private final static String FORMAT_PAUSE = "[Chunky] Task paused for %s.";
    private final static String FORMAT_CONTINUE = "[Chunky] Task continuing for %s.";
    private final static String FORMAT_WORLD = "[Chunky] World changed to %s.";
    private final static String FORMAT_CENTER = "[Chunky] Center changed to %d, %d.";
    private final static String FORMAT_RADIUS = "[Chunky] Radius changed to %d.";
    private final static String FORMAT_SILENT = "[Chunky] Silent mode %s.";
    private final static String FORMAT_QUIET = "[Chunky] Quiet interval set to %d seconds.";
    private final static String ERROR_SPIGOT_1_13 = "This plugin is not compatible with Spigot 1.13! Please use Paper instead.";
    private final static String ERROR_BELOW_1_13 = "This plugin is not compatible with Minecraft versions below 1.13.";

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
            return Arrays.asList("start", "pause", "continue", "world", "center", "radius", "silent", "quiet");
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
            genTask.cancel();
            sender.sendMessage(String.format(FORMAT_PAUSE, genTask.getWorld().getName()));
        }
    }

    private void cont(CommandSender sender) {
        configStorage.loadTasks().forEach(genTask -> {
            if (!genTasks.containsKey(genTask.getWorld())) {
                genTasks.put(genTask.getWorld(), genTask);
                this.getServer().getScheduler().runTaskAsynchronously(this, genTask);
                sender.sendMessage(String.format(FORMAT_CONTINUE, world.getName()));
            } else {
                sender.sendMessage(String.format(FORMAT_STARTED_ALREADY, world.getName()));
            }
        });
    }

    private void cancel(CommandSender sender) {
        pause(sender);
        this.getConfigStorage().reset();
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
