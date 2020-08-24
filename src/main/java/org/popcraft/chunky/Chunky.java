package org.popcraft.chunky;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.papermc.lib.PaperLib;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitWorker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Chunky extends JavaPlugin {
    private ConfigStorage configStorage;
    private ConcurrentHashMap<World, GenerationTask> generationTasks;
    private Map<String, String> translations, fallbackTranslations;
    private World world;
    private int x, z, radius;
    private String pattern, shape;
    private boolean silent;
    private int quiet;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.getConfig().options().copyHeader(true);
        this.saveConfig();
        this.configStorage = new ConfigStorage(this);
        this.generationTasks = new ConcurrentHashMap<>();
        final String language = this.getConfig().getString("language", "en");
        this.translations = loadTranslation(language);
        this.fallbackTranslations = loadTranslation("en");
        this.world = this.getServer().getWorlds().get(0);
        this.x = 0;
        this.z = 0;
        this.radius = 500;
        this.pattern = "concentric";
        this.shape = "square";
        this.silent = false;
        this.quiet = 1;
        Metrics metrics = new Metrics(this, 8211);
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> language));
        if (BukkitVersion.v1_13_2.isEqualTo(BukkitVersion.getCurrent()) && !PaperLib.isPaper()) {
            this.getLogger().severe(message("error_version_spigot"));
            this.getServer().getPluginManager().disablePlugin(this);
        } else if (BukkitVersion.v1_13_2.isHigherThan(BukkitVersion.getCurrent())) {
            this.getLogger().severe(message("error_version"));
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
            sender.sendMessage(message("help_menu",
                    message("help_start"), message("help_pause"), message("help_continue"),
                    message("help_cancel"), message("help_world"), message("help_center"),
                    message("help_radius"), message("help_silent"), message("help_quiet")));
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
            case "pattern":
                pattern(sender, args);
                break;
            case "shape":
                shape(sender, args);
                break;
            default:
                sender.sendMessage(message("help_menu",
                        message("help_start"), message("help_pause"), message("help_continue"),
                        message("help_cancel"), message("help_world"), message("help_center"),
                        message("help_radius"), message("help_silent"), message("help_quiet")));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("start", "pause", "continue", "world", "worldborder", "center", "radius", "silent", "quiet", "pattern", "shape"));
        } else if (args.length == 2 && "world".equalsIgnoreCase(args[0])) {
            this.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
        } else if (args.length == 2 && "pattern".equalsIgnoreCase(args[0])) {
            suggestions.addAll(Arrays.asList("concentric", "loop", "spiral"));
        } else if (args.length == 2 && "shape".equalsIgnoreCase(args[0])) {
            suggestions.addAll(Arrays.asList("circle", "diamond", "pentagon", "square", "star", "triangle"));
        } else {
            return suggestions;
        }
        return suggestions.stream().filter(s -> s.toLowerCase().contains(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }

    private void start(CommandSender sender) {
        if (generationTasks.containsKey(world)) {
            sender.sendMessage(message("format_started_already", world.getName()));
            return;
        }
        GenerationTask generationTask = new GenerationTask(this, world, radius, x, z, pattern, shape);
        generationTasks.put(world, generationTask);
        this.getServer().getScheduler().runTaskAsynchronously(this, generationTask);
        sender.sendMessage(message("format_start", world.getName(), x, z, radius));
    }

    private void pause(CommandSender sender) {
        for (GenerationTask generationTask : generationTasks.values()) {
            generationTask.stop(false);
            sender.sendMessage(message("format_pause", generationTask.getWorld().getName()));
        }
    }

    private void cont(CommandSender sender) {
        configStorage.loadTasks().forEach(generationTask -> {
            if (!generationTasks.containsKey(generationTask.getWorld())) {
                generationTasks.put(generationTask.getWorld(), generationTask);
                this.getServer().getScheduler().runTaskAsynchronously(this, generationTask);
                sender.sendMessage(message("format_continue", generationTask.getWorld().getName()));
            } else {
                sender.sendMessage(message("format_started_already", generationTask.getWorld().getName()));
            }
        });
    }

    private void cancel(CommandSender sender) {
        sender.sendMessage(message("format_cancel"));
        configStorage.cancelTasks();
        generationTasks.values().forEach(generationTask -> generationTask.stop(true));
        generationTasks.clear();
        this.getServer().getScheduler().cancelTasks(this);
    }

    private void world(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(message("help_world"));
            return;
        }
        Optional<World> newWorld = Input.tryWorld(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        if (!newWorld.isPresent()) {
            sender.sendMessage(message("help_world"));
            return;
        }
        this.world = newWorld.get();
        sender.sendMessage(message("format_world", world.getName()));
    }

    private void worldBorder(CommandSender sender) {
        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        this.x = center.getBlockX();
        this.z = center.getBlockZ();
        this.radius = (int) border.getSize() / 2;
        sender.sendMessage(message("format_center", x, z));
        sender.sendMessage(message("format_radius", radius));
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
            sender.sendMessage(message("help_center"));
            return;
        }
        this.x = newX.get();
        this.z = newZ.get();
        sender.sendMessage(message("format_center", x, z));
    }

    private void radius(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(message("help_radius"));
            return;
        }
        Optional<Integer> newRadius = Input.tryInteger(args[1]);
        if (!newRadius.isPresent()) {
            sender.sendMessage(message("help_radius"));
            return;
        }
        this.radius = newRadius.get();
        sender.sendMessage(message("format_radius", radius));
    }

    private void silent(CommandSender sender) {
        this.silent = !silent;
        sender.sendMessage(message("format_silent", silent ? message("enabled") : message("disabled")));
    }

    private void quiet(CommandSender sender, String[] args) {
        Optional<Integer> newQuiet = Optional.empty();
        if (args.length > 1) {
            newQuiet = Input.tryInteger(args[1]);
        }
        if (!newQuiet.isPresent()) {
            sender.sendMessage(message("help_quiet"));
            return;
        }
        this.quiet = newQuiet.get();
        sender.sendMessage(message("format_quiet", quiet));
    }

    private void pattern(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(message("help_pattern"));
            return;
        }
        String pattern = args[1].toLowerCase();
        if (!"concentric".equals(pattern) && !"loop".equals(pattern) && !"spiral".equals(pattern)) {
            sender.sendMessage(message("help_pattern"));
            return;
        }
        this.pattern = pattern;
        sender.sendMessage(message("format_pattern", pattern));
    }

    private void shape(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(message("help_shape"));
            return;
        }
        String shape = args[1].toLowerCase();
        if (!"circle".equals(shape) && !"diamond".equals(shape) && !"pentagon".equals(shape)
                && !"square".equals(shape) && !"star".equals(shape) && !"triangle".equals(shape)) {
            sender.sendMessage(message("help_shape"));
            return;
        }
        this.shape = shape;
        sender.sendMessage(message("format_shape", shape));
    }

    private Map<String, String> loadTranslation(String language) {
        InputStream input = this.getResource("lang/" + language + ".json");
        if (input == null) {
            input = this.getResource("lang/en.json");
        }
        if (input != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                StringBuilder lang = new StringBuilder();
                String s;
                while ((s = reader.readLine()) != null) {
                    lang.append(s);
                }
                return new Gson().fromJson(lang.toString(), new TypeToken<HashMap<String, String>>() {
                }.getType());
            } catch (Exception ignored) {
            }
        }
        return Collections.emptyMap();
    }

    public String message(String key, Object... args) {
        String message = translations.getOrDefault(key, fallbackTranslations.getOrDefault(key, "Missing translation"));
        return ChatColor.translateAlternateColorCodes('&', String.format(message, args));
    }

    public ConfigStorage getConfigStorage() {
        return configStorage;
    }

    public ConcurrentHashMap<World, GenerationTask> getGenerationTasks() {
        return generationTasks;
    }

    public boolean isSilent() {
        return silent;
    }

    public int getQuiet() {
        return quiet;
    }
}
