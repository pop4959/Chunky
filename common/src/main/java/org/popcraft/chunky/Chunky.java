package org.popcraft.chunky;

import org.popcraft.chunky.command.*;
import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.Server;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.util.PendingAction;
import org.popcraft.chunky.util.Translator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Chunky {
    private Server server;
    private Config config;
    private Map<World, GenerationTask> generationTasks;
    private Map<String, ChunkyCommand> commands;
    private Selection.Builder selection;
    private Options options;
    private final Map<String, PendingAction> pendingActions = new HashMap<>();

    public Chunky(Server server) {
        this.server = server;
        this.generationTasks = new ConcurrentHashMap<>();
        this.selection = Selection.builder(server.getWorlds().get(0));
        this.options = new Options();
    }

    public void loadCommands() {
        final Map<String, ChunkyCommand> commands = new HashMap<>();
        commands.put("cancel", new CancelCommand(this));
        commands.put("center", new CenterCommand(this));
        commands.put("confirm", new ConfirmCommand(this));
        commands.put("continue", new ContinueCommand(this));
        commands.put("corners", new CornersCommand(this));
        commands.put("help", new HelpCommand(this));
        commands.put("pattern", new PatternCommand(this));
        commands.put("pause", new PauseCommand(this));
        commands.put("progress", new ProgressCommand(this));
        commands.put("quiet", new QuietCommand(this));
        commands.put("radius", new RadiusCommand(this));
        commands.put("reload", new ReloadCommand(this));
        commands.put("shape", new ShapeCommand(this));
        commands.put("silent", new SilentCommand(this));
        commands.put("spawn", new SpawnCommand(this));
        commands.put("start", new StartCommand(this));
        commands.put("trim", new TrimCommand(this));
        commands.put("worldborder", new WorldBorderCommand(this));
        commands.put("world", new WorldCommand(this));
        this.commands = commands;
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
}
