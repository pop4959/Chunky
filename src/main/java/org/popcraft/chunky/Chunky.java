package org.popcraft.chunky;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;
import static org.popcraft.chunky.Constants.*;

public final class Chunky extends JavaPlugin {
    private ConfigStorage configStorage;
    private ConcurrentHashMap<World, GenTask> genTasks;
    private World world;
    private int x, z, radius;
    private boolean queue;
    private boolean silent;
    private int quiet;
    private Metrics metrics;

    private final Map<String, ChunkyCommand> commandMap = new HashMap<>();

    @Override
    public void onEnable() {
        this.configStorage = new ConfigStorage(this);
        this.genTasks = new ConcurrentHashMap<>();
        this.world = this.getServer().getWorlds().get(0);
        this.x = 0;
        this.z = 0;
        this.radius = 500;
        this.silent = false;
        this.quiet = 0;
        this.metrics = new Metrics(this, 8211);

        commandMap.put("start", this::start);
        commandMap.put("pause", this::pause);
        commandMap.put("continue", this::cont);
        commandMap.put("cancel", this::cancel);
        commandMap.put("world", this::world);
        commandMap.put("center", this::center);
        commandMap.put("radius", this::radius);
        commandMap.put("silent", this::silent);
        commandMap.put("quiet", this::quiet);
    }

    @Override
    public void onDisable() {
        pause(this.getServer().getConsoleSender(), null);
        this.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String commandString = Optional.of(args[0])
                .map(String::toLowerCase)
                .orElse("");

        commandMap.getOrDefault(commandString, this::defaultCommand)
                .accept(sender, args);

        return true;
    }

    private void defaultCommand(CommandSender sender, String[] strings) {
        sender.sendMessage(HELP_MENU);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(commandMap.keySet());
        }

        if (args.length == 2 && "world".equalsIgnoreCase(args[0])) {
            final String worldName = Optional.ofNullable(args[1])
                    .map(String::toLowerCase)
                    .orElse("");

            return getWorldNames(worldName);
        }

        return Collections.emptyList();
    }

    private List<String> getWorldNames(String worldName) {
        final String worldNameLowerCase = worldName.toLowerCase();

        return Bukkit.getWorlds()
                .stream()
                .map(World::getName)
                .map(String::toLowerCase)
                .filter(w -> w.startsWith(worldNameLowerCase))
                .collect(toList());
    }

    private void start(CommandSender sender, String[] args) {
        if (genTasks.containsKey(world)) {
            sender.sendMessage(String.format(FORMAT_STARTED_ALREADY, world.getName()));
            return;
        }

        GenTask genTask = new GenTask(this, world, radius, x, z);
        genTasks.put(world, genTask);
        this.getServer().getScheduler().runTaskAsynchronously(this, genTask);
        sender.sendMessage(String.format(FORMAT_START, world.getName(), x, z, radius));
    }

    private void pause(CommandSender sender, String[] args) {
        for (GenTask genTask : genTasks.values()) {
            genTask.cancel();
            sender.sendMessage(String.format(FORMAT_PAUSE, genTask.getWorld().getName()));
        }
    }

    private void cont(CommandSender sender, String[] args) {
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

    private void cancel(CommandSender sender, String[] args) {
        pause(sender, args);
        this.getConfigStorage().reset();
        this.getServer().getScheduler().cancelTasks(this);
    }

    private void world(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(HELP_WORLD);
            return;
        }
        Optional<World> newWorld = Input.tryWorld(args[1]);
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

    private void silent(CommandSender sender, String[] args) {
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
