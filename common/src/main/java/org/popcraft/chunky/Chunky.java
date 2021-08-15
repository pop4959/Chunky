package org.popcraft.chunky;

import org.popcraft.chunky.command.*;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.Server;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.PendingAction;
import org.popcraft.chunky.util.RegionCache;
import org.popcraft.chunky.util.TaskScheduler;
import org.popcraft.chunky.util.Translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Chunky {
    private final Server server;
    private final Config config;
    private final Selection.Builder selection;
    private final TaskScheduler scheduler = new TaskScheduler();
    private final Map<World, GenerationTask> generationTasks = new ConcurrentHashMap<>();
    private final Options options = new Options();
    private final Map<String, PendingAction> pendingActions = new HashMap<>();
    private final RegionCache regionCache = new RegionCache();
    private final double limit;
    private final Map<String, ChunkyCommand> commands;

    public Chunky(Server server, Config config) {
        this.server = server;
        this.config = config;
        this.selection = Selection.builder(server.getWorlds().get(0));
        this.limit = loadLimit().orElse(Double.MAX_VALUE);
        this.commands = loadCommands();
    }

    private Map<String, ChunkyCommand> loadCommands() {
        final Map<String, ChunkyCommand> commandMap = new HashMap<>();
        commandMap.put("cancel", new CancelCommand(this));
        commandMap.put("center", new CenterCommand(this));
        commandMap.put("confirm", new ConfirmCommand(this));
        commandMap.put("continue", new ContinueCommand(this));
        commandMap.put("corners", new CornersCommand(this));
        commandMap.put("help", new HelpCommand(this));
        commandMap.put("pattern", new PatternCommand(this));
        commandMap.put("pause", new PauseCommand(this));
        commandMap.put("progress", new ProgressCommand(this));
        commandMap.put("quiet", new QuietCommand(this));
        commandMap.put("radius", new RadiusCommand(this));
        commandMap.put("reload", new ReloadCommand(this));
        commandMap.put("shape", new ShapeCommand(this));
        commandMap.put("silent", new SilentCommand(this));
        commandMap.put("spawn", new SpawnCommand(this));
        commandMap.put("start", new StartCommand(this));
        commandMap.put("trim", new TrimCommand(this));
        commandMap.put("worldborder", new WorldBorderCommand(this));
        commandMap.put("world", new WorldCommand(this));
        return commandMap;
    }

    private Optional<Double> loadLimit() {
        final Path limitFile = config.getDirectory().resolve(".chunky.properties");
        try (final InputStream input = Files.newInputStream(limitFile)) {
            final Properties properties = new Properties();
            properties.load(input);
            return Input.tryDouble(properties.getProperty("radius-limit"));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void disable() {
        getConfig().saveTasks();
        getGenerationTasks().values().forEach(generationTask -> generationTask.stop(false));
        getScheduler().cancelTasks();
    }

    public TaskScheduler getScheduler() {
        return scheduler;
    }

    public Server getServer() {
        return server;
    }

    public Config getConfig() {
        return config;
    }

    public Map<World, GenerationTask> getGenerationTasks() {
        return generationTasks;
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

    public void setLanguage(String language) {
        Translator.setLanguage(language);
    }

    public RegionCache getRegionCache() {
        return regionCache;
    }

    public double getLimit() {
        return limit;
    }
}
