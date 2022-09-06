package org.popcraft.chunky;

import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.api.ChunkyAPIImpl;
import org.popcraft.chunky.command.CancelCommand;
import org.popcraft.chunky.command.CenterCommand;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.ConfirmCommand;
import org.popcraft.chunky.command.ContinueCommand;
import org.popcraft.chunky.command.CornersCommand;
import org.popcraft.chunky.command.HelpCommand;
import org.popcraft.chunky.command.PatternCommand;
import org.popcraft.chunky.command.PauseCommand;
import org.popcraft.chunky.command.ProgressCommand;
import org.popcraft.chunky.command.QuietCommand;
import org.popcraft.chunky.command.RadiusCommand;
import org.popcraft.chunky.command.ReloadCommand;
import org.popcraft.chunky.command.SelectionCommand;
import org.popcraft.chunky.command.ShapeCommand;
import org.popcraft.chunky.command.SilentCommand;
import org.popcraft.chunky.command.SpawnCommand;
import org.popcraft.chunky.command.StartCommand;
import org.popcraft.chunky.command.TrimCommand;
import org.popcraft.chunky.command.WorldBorderCommand;
import org.popcraft.chunky.command.WorldCommand;
import org.popcraft.chunky.event.EventBus;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.Server;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.PendingAction;
import org.popcraft.chunky.util.RegionCache;
import org.popcraft.chunky.util.TaskLoader;
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
    private final TaskLoader taskLoader;
    private final EventBus eventBus;
    private final Selection.Builder selection;
    private final TaskScheduler scheduler = new TaskScheduler();
    private final Map<String, GenerationTask> generationTasks = new ConcurrentHashMap<>();
    private final Map<String, PendingAction> pendingActions = new HashMap<>();
    private final RegionCache regionCache = new RegionCache();
    private final double limit;
    private final Version version;
    private final Map<String, ChunkyCommand> commands;
    private final ChunkyAPI api;

    public Chunky(final Server server, final Config config) {
        this.server = server;
        this.config = config;
        this.taskLoader = new TaskLoader(this);
        this.eventBus = new EventBus();
        this.selection = Selection.builder(this, server.getWorlds().get(0));
        this.limit = loadLimit().orElse(Double.MAX_VALUE);
        this.version = loadVersion();
        this.commands = loadCommands();
        this.api = new ChunkyAPIImpl(this);
        ChunkyProvider.register(this);
    }

    public void disable() {
        taskLoader.saveTasks();
        getGenerationTasks().values().forEach(generationTask -> generationTask.stop(false));
        getScheduler().cancelTasks();
        ChunkyProvider.unregister();
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
        commandMap.put(CommandLiteral.SELECTION, new SelectionCommand(this));
        commandMap.put(CommandLiteral.SHAPE, new ShapeCommand(this));
        commandMap.put(CommandLiteral.SILENT, new SilentCommand(this));
        commandMap.put(CommandLiteral.SPAWN, new SpawnCommand(this));
        commandMap.put(CommandLiteral.START, new StartCommand(this));
        commandMap.put(CommandLiteral.TRIM, new TrimCommand(this));
        commandMap.put(CommandLiteral.WORLDBORDER, new WorldBorderCommand(this));
        commandMap.put(CommandLiteral.WORLD, new WorldCommand(this));
        return commandMap;
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

    public TaskLoader getTaskLoader() {
        return taskLoader;
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

    public Optional<Runnable> getPendingAction(final Sender sender) {
        pendingActions.values().removeIf(PendingAction::hasExpired);
        final PendingAction pendingAction = pendingActions.remove(sender.getName());
        return Optional.ofNullable(pendingAction).map(PendingAction::getAction);
    }

    public void setPendingAction(final Sender sender, final Runnable action) {
        pendingActions.put(sender.getName(), new PendingAction(action));
    }

    public void setLanguage(final String language) {
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

    public ChunkyAPI getApi() {
        return api;
    }
}
