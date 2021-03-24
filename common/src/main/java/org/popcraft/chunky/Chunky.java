package org.popcraft.chunky;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.popcraft.chunky.command.*;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Platform;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.PendingAction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Chunky {
    private static Map<String, String> translations = Collections.emptyMap();
    private static Map<String, String> fallbackTranslations = Collections.emptyMap();
    private Platform platform;
    private Config config;
    private Map<World, GenerationTask> generationTasks;
    private Map<String, ChunkyCommand> commands;
    private Selection.Builder selection;
    private Options options;
    private final Map<String, PendingAction> pendingActions = new HashMap<>();

    public Chunky(Platform platform) {
        this.platform = platform;
        this.generationTasks = new ConcurrentHashMap<>();
        this.selection = Selection.builder(platform.getServer().getWorlds().get(0));
        this.options = new Options();
    }

    public void loadCommands() {
        final Map<String, ChunkyCommand> commands = new HashMap<>();
        commands.put("cancel", new CancelCommand(this));
        commands.put("center", new CenterCommand(this));
        commands.put("confirm", new ConfirmCommand(this));
        commands.put("continue", new ContinueCommand(this));
        commands.put("corners", new CornersCommand(this));
        commands.put("delete", new DeleteCommand(this));
        commands.put("help", new HelpCommand(this));
        commands.put("pattern", new PatternCommand(this));
        commands.put("pause", new PauseCommand(this));
        commands.put("quiet", new QuietCommand(this));
        commands.put("radius", new RadiusCommand(this));
        commands.put("reload", new ReloadCommand(this));
        commands.put("shape", new ShapeCommand(this));
        commands.put("silent", new SilentCommand(this));
        commands.put("spawn", new SpawnCommand(this));
        commands.put("start", new StartCommand(this));
        commands.put("worldborder", new WorldBorderCommand(this));
        commands.put("world", new WorldCommand(this));
        this.commands = commands;
    }

    public Map<String, String> loadTranslation(InputStream input) {
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

    public static String translate(String key, Object... args) {
        String message = translations.getOrDefault(key, fallbackTranslations.getOrDefault(key, "Missing translation"));
        return String.format(message, args);
    }

    public Platform getPlatform() {
        return platform;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Map<World, GenerationTask> getGenerationTasks() {
        return generationTasks;
    }

    public void setTranslations(Map<String, String> translations) {
        Chunky.translations = translations;
    }

    public void setFallbackTranslations(Map<String, String> fallbackTranslations) {
        Chunky.fallbackTranslations = fallbackTranslations;
    }

    public Map<String, ChunkyCommand> getCommands() {
        return commands;
    }

    public Selection.Builder getSelection() {
        return selection;
    }

    public Options getOptions() {
        return options;
    }

    public Optional<Runnable> getPendingAction(Sender sender) {
        pendingActions.values().removeIf(PendingAction::hasExpired);
        PendingAction pendingAction = pendingActions.remove(sender.getName());
        return Optional.ofNullable(pendingAction).map(PendingAction::getAction);
    }

    public void setPendingAction(Sender sender, Runnable action) {
        pendingActions.put(sender.getName(), new PendingAction(action));
    }
}
