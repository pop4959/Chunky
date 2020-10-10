package org.popcraft.chunky;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.papermc.lib.PaperLib;
import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.command.*;
import org.popcraft.chunky.task.BukkitTaskManager;
import org.popcraft.chunky.task.TaskManager;
import org.popcraft.chunky.util.Version;

import co.aikar.commands.PaperCommandManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Chunky extends JavaPlugin {
    private ConfigStorage configStorage;
    private TaskManager taskManager;
    private Map<String, String> translations, fallbackTranslations;
    private Map<String, ChunkyCommand> commands;
    private Selection selection;
    private @Getter PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.getConfig().options().copyHeader(true);
        this.saveConfig();
        this.configStorage = new ConfigStorage(this);
        this.taskManager = new BukkitTaskManager();
        this.translations = loadTranslation(getConfig().getString("language", "en"));
        this.fallbackTranslations = loadTranslation("en");
        this.commands = loadCommands();
        this.commandManager = new PaperCommandManager(this);
        this.commandManager.registerCommand(new HynixChunkyCommand(this));

        this.selection = new Selection();
        Metrics metrics = new Metrics(this, 8211);
        if (metrics.isEnabled()) {
            metrics.addCustomChart(new Metrics.SimplePie("language", () -> getConfig().getString("language", "en")));
        }
        Version currentVersion = Version.getCurrentMinecraftVersion();
        if (Version.v1_13_2.isEqualTo(currentVersion) && !PaperLib.isPaper()) {
            this.getLogger().severe(message("error_version_spigot"));
            this.getServer().getPluginManager().disablePlugin(this);
        } else if (Version.v1_13_2.isHigherThan(currentVersion)) {
            this.getLogger().severe(message("error_version"));
            this.getServer().getPluginManager().disablePlugin(this);
        }
        if (this.getConfig().getBoolean("continue-on-restart", false)) {
            commands.get("continue").execute(getServer().getConsoleSender(), new String[] {});
        }
    }

    @Override
    public void onDisable() {
        this.getTaskManager().stopAll(true, false, true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && commands.containsKey(args[0].toLowerCase())) {
            commands.get(args[0].toLowerCase()).execute(sender, args);
        } else {
            commands.get("help").execute(sender, new String[] {});
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 1) {
            return Collections.emptyList();
        }
        final List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.addAll(commands.keySet());
        } else if (commands.containsKey(args[0].toLowerCase())) {
            suggestions.addAll(commands.get(args[0].toLowerCase()).tabSuggestions(sender, args));
        }
        return suggestions.stream().filter(s -> s.toLowerCase().contains(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private Map<String, ChunkyCommand> loadCommands() {
        Map<String, ChunkyCommand> commands = new HashMap<>();
        commands.put("cancel", new CancelCommand(this));
        commands.put("center", new CenterCommand(this));
        commands.put("continue", new ContinueCommand(this));
        commands.put("help", new HelpCommand(this));
        commands.put("pattern", new PatternCommand(this));
        commands.put("pause", new PauseCommand(this));
        commands.put("quiet", new QuietCommand(this));
        commands.put("radius", new RadiusCommand(this));
        commands.put("shape", new ShapeCommand(this));
        commands.put("silent", new SilentCommand(this));
        commands.put("start", new StartCommand(this));
        commands.put("worldborder", new WorldBorderCommand(this));
        commands.put("world", new WorldCommand(this));
        return commands;
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

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public Selection getSelection() {
        return selection;
    }
}
