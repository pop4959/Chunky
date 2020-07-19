package org.popcraft.chunky;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.popcraft.chunky.Constants.*;

public final class Chunky extends JavaPlugin {
    private ConfigStorage configStorage;
    private ConcurrentHashMap<World, GenTask> genTasks;
    private World world;
    private int x, z, radius;
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

        commandMap.put("start", this::startCommand);
        commandMap.put("pause", this::pauseCommand);
        commandMap.put("continue", this::continueCommand);
        commandMap.put("cancel", this::cancelCommand);
        commandMap.put("world", this::worldCommand);
        commandMap.put("center", this::centerCommand);
        commandMap.put("radius", this::radiusCommand);
        commandMap.put("silent", this::silentCommand);
        commandMap.put("quiet", this::quietCommand);
    }

    @Override
    public void onDisable() {
        pauseCommand(this.getServer().getConsoleSender(), null);
        this.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final CommandArguments arguments = new CommandArguments(args);

        final String commandString = arguments.getString(0)
                .map(String::toLowerCase)
                .orElse("");

        commandMap.getOrDefault(commandString, this::unknownCommand)
                .accept(sender, arguments);

        return true;
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

            return Utils.getWorldNamesFilteredByName(worldName);
        }

        return Collections.emptyList();
    }

    private void unknownCommand(CommandSender sender, CommandArguments args) {
        sender.sendMessage(HELP_MENU);
    }

    private void startCommand(CommandSender sender, CommandArguments args) {
        if (genTasks.containsKey(world)) {
            sender.sendMessage(String.format(FORMAT_STARTED_ALREADY, world.getName()));
            return;
        }

        GenTask genTask = new GenTask(this, world, radius, x, z);
        genTasks.put(world, genTask);
        this.getServer().getScheduler().runTaskAsynchronously(this, genTask);
        sender.sendMessage(String.format(FORMAT_START, world.getName(), x, z, radius));
    }

    private void pauseCommand(CommandSender sender, CommandArguments args) {
        for (GenTask genTask : genTasks.values()) {
            genTask.cancel();
            sender.sendMessage(String.format(FORMAT_PAUSE, genTask.getWorld().getName()));
        }
    }

    private void continueCommand(CommandSender sender, CommandArguments args) {
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

    private void cancelCommand(CommandSender sender, CommandArguments args) {
        pauseCommand(sender, args);
        this.getConfigStorage().reset();
        this.getServer().getScheduler().cancelTasks(this);
    }

    private void worldCommand(CommandSender sender, CommandArguments args) {
        if (args.size() < 2) {
            sender.sendMessage(HELP_WORLD);
            return;
        }

        Optional<World> newWorld = args.getString(1)
                .map(Bukkit::getWorld);

        if (!newWorld.isPresent()) {
            sender.sendMessage(HELP_WORLD);
            return;
        }
        this.world = newWorld.get();
        sender.sendMessage(String.format(FORMAT_WORLD, world.getName()));
    }

    private void centerCommand(CommandSender sender, CommandArguments args) {
        Optional<Integer> newX = args.getInt(1);
        Optional<Integer> newZ = args.getInt(2);

        if (!newX.isPresent() || !newZ.isPresent()) {
            sender.sendMessage(HELP_CENTER);
            return;
        }

        this.x = newX.get();
        this.z = newZ.get();
        sender.sendMessage(String.format(FORMAT_CENTER, x, z));
    }

    private void radiusCommand(CommandSender sender, CommandArguments args) {
        if (args.size() < 2) {
            sender.sendMessage(HELP_RADIUS);
            return;
        }

        Optional<Integer> newRadius = args.getInt(1);

        if (!newRadius.isPresent()) {
            sender.sendMessage(HELP_RADIUS);
            return;
        }

        this.radius = newRadius.get();
        sender.sendMessage(String.format(FORMAT_RADIUS, radius));
    }

    private void silentCommand(CommandSender sender, CommandArguments args) {
        this.silent = !silent;
        sender.sendMessage(String.format(FORMAT_SILENT, silent ? "enabled" : "disabled"));
    }

    private void quietCommand(CommandSender sender, CommandArguments args) {
        Optional<Integer> newQuiet = args.getInt(1);

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
