package org.popcraft.chunky;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.platform.SpongeConfig;
import org.popcraft.chunky.platform.SpongeSender;
import org.popcraft.chunky.platform.SpongeServer;
import org.popcraft.chunky.util.Limit;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Plugin("chunky")
public class ChunkySponge {
    private Chunky chunky;
    private PluginContainer container;
    private Logger logger;
    @Inject
    private Game game;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configPath;

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        this.container = event.plugin();
        this.logger = event.plugin().logger();
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
    }

    @Listener
    public void onLoadedGame(final LoadedGameEvent event) {
        this.chunky = new Chunky(new SpongeServer(this));
        chunky.setConfig(new SpongeConfig(this));
        chunky.setLanguage(chunky.getConfig().getLanguage());
        chunky.loadCommands();
        Limit.set(chunky.getConfig());
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {
        chunky.getConfig().saveTasks();
        chunky.getGenerationTasks().values().forEach(generationTask -> generationTask.stop(false));
        chunky.getServer().getScheduler().cancelTasks();
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        Command.Parameterized cancelCommand = Command.builder()
                .permission("chunky.command.cancel")
                .addParameters(Parameter.world().key("world").terminal().build())
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("cancel");
                    ctx.one(Parameter.key("world", ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    executeSpongeCommand(ctx, "cancel", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized centerCommand = Command.builder()
                .permission("chunky.command.center")
                .addParameters(
                        Parameter.string().key("x").terminal().build(),
                        Parameter.string().key("z").terminal().build()
                )
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("center");
                    ctx.one(Parameter.key("x", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("z", String.class)).ifPresent(args::add);
                    executeSpongeCommand(ctx, "center", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized confirmCommand = Command.builder()
                .permission("chunky.command.confirm")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, "confirm", Collections.singletonList("confirm"));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized continueCommand = Command.builder()
                .permission("chunky.command.continue")
                .addParameters(Parameter.world().key("world").terminal().build())
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("continue");
                    ctx.one(Parameter.key("world", ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    executeSpongeCommand(ctx, "continue", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized cornersCommand = Command.builder()
                .permission("chunky.command.corners")
                .addParameters(
                        Parameter.string().key("x1").build(),
                        Parameter.string().key("z1").build(),
                        Parameter.string().key("x2").build(),
                        Parameter.string().key("z2").build()
                )
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("corners");
                    args.add(ctx.requireOne(Parameter.key("x1", String.class)));
                    args.add(ctx.requireOne(Parameter.key("z1", String.class)));
                    args.add(ctx.requireOne(Parameter.key("x2", String.class)));
                    args.add(ctx.requireOne(Parameter.key("z2", String.class)));
                    executeSpongeCommand(ctx, "corners", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized helpCommand = Command.builder()
                .permission("chunky.command.help")
                .addParameters(Parameter.integerNumber().key("page").terminal().build())
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("help");
                    ctx.one(Parameter.key("page", Integer.class)).map(String::valueOf).ifPresent(args::add);
                    executeSpongeCommand(ctx, "help", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized patternCommand = Command.builder()
                .permission("chunky.command.pattern")
                .addParameters(Parameter.string().key("pattern").completer(SuggestionProviders.PATTERNS).build())
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("pattern");
                    args.add(ctx.requireOne(Parameter.key("pattern", String.class)));
                    executeSpongeCommand(ctx, "pattern", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized pauseCommand = Command.builder()
                .permission("chunky.command.pause")
                .addParameters(Parameter.world().key("world").terminal().build())
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("pause");
                    ctx.one(Parameter.key("world", ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    executeSpongeCommand(ctx, "pause", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized quietCommand = Command.builder()
                .permission("chunky.command.quiet")
                .addParameters(Parameter.integerNumber().key("interval").build())
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("quiet");
                    args.add(String.valueOf(ctx.requireOne(Parameter.key("interval", Integer.class))));
                    executeSpongeCommand(ctx, "quiet", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized radiusCommand = Command.builder()
                .permission("chunky.command.radius")
                .addParameters(
                        Parameter.string().key("radiusX").terminal().build(),
                        Parameter.string().key("radiusZ").terminal().build()
                )
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("radius");
                    args.add(ctx.requireOne(Parameter.key("radiusX", String.class)));
                    ctx.one(Parameter.key("radiusZ", String.class)).ifPresent(args::add);
                    executeSpongeCommand(ctx, "radius", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized reloadCommand = Command.builder()
                .permission("chunky.command.reload")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, "reload", Collections.singletonList("reload"));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized shapeCommand = Command.builder()
                .permission("chunky.command.shape")
                .addParameters(Parameter.string().key("shape").completer(SuggestionProviders.SHAPES).build())
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("shape");
                    args.add(ctx.requireOne(Parameter.key("shape", String.class)));
                    executeSpongeCommand(ctx, "shape", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized silentCommand = Command.builder()
                .permission("chunky.command.silent")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, "silent", Collections.singletonList("silent"));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized spawnCommand = Command.builder()
                .permission("chunky.command.spawn")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, "spawn", Collections.singletonList("spawn"));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized startCommand = Command.builder()
                .permission("chunky.command.start")
                .addParameters(
                        Parameter.world().key("world").terminal().build(),
                        Parameter.string().key("shape").terminal().completer(SuggestionProviders.SHAPES).build(),
                        Parameter.string().key("x").terminal().build(),
                        Parameter.string().key("z").terminal().build(),
                        Parameter.string().key("radiusX").terminal().build(),
                        Parameter.string().key("radiusZ").terminal().build()
                )
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("start");
                    ctx.one(Parameter.key("world", ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    ctx.one(Parameter.key("shape", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("x", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("z", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("radiusX", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("radiusZ", String.class)).ifPresent(args::add);
                    executeSpongeCommand(ctx, "start", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized trimCommand = Command.builder()
                .permission("chunky.command.trim")
                .addParameters(
                        Parameter.world().key("world").terminal().build(),
                        Parameter.string().key("shape").terminal().completer(SuggestionProviders.SHAPES).build(),
                        Parameter.string().key("x").terminal().build(),
                        Parameter.string().key("z").terminal().build(),
                        Parameter.string().key("radiusX").terminal().build(),
                        Parameter.string().key("radiusZ").terminal().build()
                )
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("trim");
                    ctx.one(Parameter.key("world", ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    ctx.one(Parameter.key("shape", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("x", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("z", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("radiusX", String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key("radiusZ", String.class)).ifPresent(args::add);
                    executeSpongeCommand(ctx, "trim", args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized worldborderCommand = Command.builder()
                .permission("chunky.command.worldborder")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, "worldborder", Collections.singletonList("worldborder"));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized worldCommand = Command.builder()
                .permission("chunky.command.world")
                .addParameters(Parameter.world().key("world").build())
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add("world");
                    ctx.one(Parameter.key("world", ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    executeSpongeCommand(ctx, "world", args);
                    return CommandResult.success();
                })
                .build();
        event.register(this.container, Command.builder()
                .permission("chunky.command.base")
                .addChild(cancelCommand, "cancel")
                .addChild(centerCommand, "center")
                .addChild(confirmCommand, "confirm")
                .addChild(continueCommand, "continue")
                .addChild(cornersCommand, "corners")
                .addChild(helpCommand, "help")
                .addChild(patternCommand, "pattern")
                .addChild(pauseCommand, "pause")
                .addChild(quietCommand, "quiet")
                .addChild(radiusCommand, "radius")
                .addChild(reloadCommand, "reload")
                .addChild(shapeCommand, "shape")
                .addChild(silentCommand, "silent")
                .addChild(spawnCommand, "spawn")
                .addChild(startCommand, "start")
                .addChild(trimCommand, "trim")
                .addChild(worldborderCommand, "worldborder")
                .addChild(worldCommand, "world")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, "help", Collections.emptyList());
                    return CommandResult.success();
                })
                .build(), "chunky");
    }

    private void executeSpongeCommand(final CommandContext ctx, final String command, final List<String> args) {
        final String[] argsArray = new String[args.size()];
        chunky.getCommands().get(command).execute(new SpongeSender(ctx.cause().audience()), args.toArray(argsArray));
    }

    public Chunky getChunky() {
        return chunky;
    }

    public PluginContainer getContainer() {
        return container;
    }

    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public Path getConfigPath() {
        return configPath;
    }
}
