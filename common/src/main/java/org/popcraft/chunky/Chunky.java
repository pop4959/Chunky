package org.popcraft.chunky;

import org.popcraft.chunky.command.*;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.Server;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.PendingAction;
import org.popcraft.chunky.util.RegionCache;
import org.popcraft.chunky.util.Translator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Chunky {
    private final Server server;
    private final Map<World, GenerationTask> generationTasks;
    private final Selection.Builder selection;
    private final Options options;
    private final Map<String, PendingAction> pendingActions = new HashMap<>();
    private final RegionCache regionCache;
    private Config config;
    private Map<String, ChunkyCommand> commands;

    public Chunky(Server server) {
        this.server = server;
        this.generationTasks = new ConcurrentHashMap<>();
        this.selection = Selection.builder(server.getWorlds().get(0));
        this.options = new Options();
        this.regionCache = new RegionCache();
    }

    public void loadCommands() {
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
        this.commands = commandMap;
    }

    public Server getServer() {
        return server;
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
}
