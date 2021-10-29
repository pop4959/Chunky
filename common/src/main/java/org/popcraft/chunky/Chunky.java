package org.popcraft.chunky;

import org.popcraft.chunky.command.*;
import org.popcraft.chunky.event.EventBus;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.Server;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.PendingAction;
import org.popcraft.chunky.util.RegionCache;
import org.popcraft.chunky.util.TaskScheduler;
import org.popcraft.chunky.util.Translator;
import org.popcraft.chunky.util.Version;

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
    private final EventBus eventBus;
    private final Selection.Builder selection;
    private final TaskScheduler scheduler = new TaskScheduler();
    private final Map<String, GenerationTask> generationTasks = new ConcurrentHashMap<>();
    private final Options options = new Options();
    private final Map<String, PendingAction> pendingActions = new HashMap<>();
    private final RegionCache regionCache = new RegionCache();
    private final double limit;
    private final Version version;
    private final Map<String, ChunkyCommand> commands;

    public Chunky(Server server, Config config) {
        this.server = server;
        this.config = config;
        this.eventBus = new EventBus();
        this.selection = Selection.builder(server.getWorlds().get(0));
        this.limit = loadLimit().orElse(Double.MAX_VALUE);
        this.version = loadVersion();
        this.commands = loadCommands();
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

    private Version loadVersion() {
        try (final InputStream input = getClass().getClassLoader().getResourceAsStream("version.properties")) {
            final Properties properties = new Properties();
            properties.load(input);
            return new Version(properties.getProperty("version"));
        } catch (IOException e) {
            return Version.INVALID;
        }
    }

    private Map<String, ChunkyCommand> loadCommands() {
        final Map<String, ChunkyCommand> commandMap = new HashMap<>();
        commandMap.put(CommandLiteral.CANCEL, new CancelCommand(this));
        commandMap.put(CommandLiteral.CENTER, new CenterCommand(this));
        commandMap.put(CommandLiteral.CONFIRM, new ConfirmCommand(this));
        commandMap.put(CommandLiteral.CONTINUE, new ContinueCommand(this));
        commandMap.put(CommandLiteral.CORNERS, new CornersCommand(this));
        commandMap.put(CommandLiteral.HELP, new HelpCommand(this));
        commandMap.put(CommandLiteral.PATTERN, new PatternCommand(this));
        commandMap.put(CommandLiteral.PAUSE, new PauseCommand(this));
        commandMap.put(CommandLiteral.PROGRESS, new ProgressCommand(this));
        commandMap.put(CommandLiteral.QUIET, new QuietCommand(this));
        commandMap.put(CommandLiteral.RADIUS, new RadiusCommand(this));
        commandMap.put(CommandLiteral.RELOAD, new ReloadCommand(this));
        commandMap.put(CommandLiteral.SHAPE, new ShapeCommand(this));
        commandMap.put(CommandLiteral.SILENT, new SilentCommand(this));
        commandMap.put(CommandLiteral.SPAWN, new SpawnCommand(this));
        commandMap.put(CommandLiteral.START, new StartCommand(this));
        commandMap.put(CommandLiteral.TRIM, new TrimCommand(this));
        commandMap.put(CommandLiteral.WORLDBORDER, new WorldBorderCommand(this));
        commandMap.put(CommandLiteral.WORLD, new WorldCommand(this));
        return commandMap;
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

    public EventBus getEventBus() {
        return eventBus;
    }

    public Map<String, GenerationTask> getGenerationTasks() {
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

    public Version getVersion() {
        return version;
    }
}
