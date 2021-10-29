package org.popcraft.chunky;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.platform.SpongeConfig;
import org.popcraft.chunky.platform.SpongeSender;
import org.popcraft.chunky.platform.SpongeServer;
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
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

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
    public void onLoadedGame(final LoadedGameEvent event) {
        this.chunky = new Chunky(new SpongeServer(this), new SpongeConfig(this));
        if (chunky.getConfig().getContinueOnRestart()) {
            chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), new String[]{});
        }
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {
        chunky.disable();
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        Command.Parameterized cancelCommand = Command.builder()
                .permission("chunky.command.cancel")
                .addParameters(Parameter.world().key(CommandLiteral.WORLD).terminal().build())
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.CANCEL);
                    ctx.one(Parameter.key(CommandLiteral.WORLD, ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.CANCEL, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized centerCommand = Command.builder()
                .permission("chunky.command.center")
                .addParameters(
                        Parameter.string().key(CommandLiteral.X).terminal().build(),
                        Parameter.string().key(CommandLiteral.Z).terminal().build()
                )
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.CENTER);
                    ctx.one(Parameter.key(CommandLiteral.X, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.Z, String.class)).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.CENTER, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized confirmCommand = Command.builder()
                .permission("chunky.command.confirm")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, CommandLiteral.CONFIRM, Collections.singletonList(CommandLiteral.CONFIRM));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized continueCommand = Command.builder()
                .permission("chunky.command.continue")
                .addParameters(Parameter.world().key(CommandLiteral.WORLD).terminal().build())
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.CONTINUE);
                    ctx.one(Parameter.key(CommandLiteral.WORLD, ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.CONTINUE, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized cornersCommand = Command.builder()
                .permission("chunky.command.corners")
                .addParameters(
                        Parameter.string().key(CommandLiteral.X1).build(),
                        Parameter.string().key(CommandLiteral.Z1).build(),
                        Parameter.string().key(CommandLiteral.X2).build(),
                        Parameter.string().key(CommandLiteral.Z2).build()
                )
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.CORNERS);
                    args.add(ctx.requireOne(Parameter.key(CommandLiteral.X1, String.class)));
                    args.add(ctx.requireOne(Parameter.key(CommandLiteral.Z1, String.class)));
                    args.add(ctx.requireOne(Parameter.key(CommandLiteral.X2, String.class)));
                    args.add(ctx.requireOne(Parameter.key(CommandLiteral.Z2, String.class)));
                    executeSpongeCommand(ctx, CommandLiteral.CORNERS, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized helpCommand = Command.builder()
                .permission("chunky.command.help")
                .addParameters(Parameter.integerNumber().key(CommandLiteral.PAGE).terminal().build())
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.HELP);
                    ctx.one(Parameter.key(CommandLiteral.PAGE, Integer.class)).map(String::valueOf).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.HELP, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized patternCommand = Command.builder()
                .permission("chunky.command.pattern")
                .addParameters(Parameter.string().key(CommandLiteral.PATTERN).completer(SuggestionProviders.PATTERNS).build())
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.PATTERN);
                    args.add(ctx.requireOne(Parameter.key(CommandLiteral.PATTERN, String.class)));
                    executeSpongeCommand(ctx, CommandLiteral.PATTERN, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized pauseCommand = Command.builder()
                .permission("chunky.command.pause")
                .addParameters(Parameter.world().key(CommandLiteral.WORLD).terminal().build())
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.PAUSE);
                    ctx.one(Parameter.key(CommandLiteral.WORLD, ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.PAUSE, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized progressCommand = Command.builder()
                .permission("chunky.command.progress")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, CommandLiteral.PROGRESS, Collections.singletonList(CommandLiteral.PROGRESS));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized quietCommand = Command.builder()
                .permission("chunky.command.quiet")
                .addParameters(Parameter.integerNumber().key(CommandLiteral.INTERVAL).build())
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.QUIET);
                    args.add(String.valueOf(ctx.requireOne(Parameter.key(CommandLiteral.INTERVAL, Integer.class))));
                    executeSpongeCommand(ctx, CommandLiteral.QUIET, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized radiusCommand = Command.builder()
                .permission("chunky.command.radius")
                .addParameters(
                        Parameter.string().key(CommandLiteral.RADIUS_X).terminal().build(),
                        Parameter.string().key(CommandLiteral.RADIUS_Z).terminal().build()
                )
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.RADIUS);
                    args.add(ctx.requireOne(Parameter.key(CommandLiteral.RADIUS_X, String.class)));
                    ctx.one(Parameter.key(CommandLiteral.RADIUS_Z, String.class)).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.RADIUS, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized reloadCommand = Command.builder()
                .permission("chunky.command.reload")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, CommandLiteral.RELOAD, Collections.singletonList(CommandLiteral.RELOAD));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized shapeCommand = Command.builder()
                .permission("chunky.command.shape")
                .addParameters(Parameter.string().key(CommandLiteral.SHAPE).completer(SuggestionProviders.SHAPES).build())
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.SHAPE);
                    args.add(ctx.requireOne(Parameter.key(CommandLiteral.SHAPE, String.class)));
                    executeSpongeCommand(ctx, CommandLiteral.SHAPE, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized silentCommand = Command.builder()
                .permission("chunky.command.silent")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, CommandLiteral.SILENT, Collections.singletonList(CommandLiteral.SILENT));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized spawnCommand = Command.builder()
                .permission("chunky.command.spawn")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, CommandLiteral.SPAWN, Collections.singletonList(CommandLiteral.SPAWN));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized startCommand = Command.builder()
                .permission("chunky.command.start")
                .addParameters(
                        Parameter.world().key(CommandLiteral.WORLD).terminal().build(),
                        Parameter.string().key(CommandLiteral.SHAPE).terminal().completer(SuggestionProviders.SHAPES).build(),
                        Parameter.string().key(CommandLiteral.X).terminal().build(),
                        Parameter.string().key(CommandLiteral.Z).terminal().build(),
                        Parameter.string().key(CommandLiteral.RADIUS_X).terminal().build(),
                        Parameter.string().key(CommandLiteral.RADIUS_Z).terminal().build()
                )
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.START);
                    ctx.one(Parameter.key(CommandLiteral.WORLD, ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.SHAPE, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.X, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.Z, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.RADIUS_X, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.RADIUS_Z, String.class)).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.START, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized trimCommand = Command.builder()
                .permission("chunky.command.trim")
                .addParameters(
                        Parameter.world().key(CommandLiteral.WORLD).terminal().build(),
                        Parameter.string().key(CommandLiteral.SHAPE).terminal().completer(SuggestionProviders.SHAPES).build(),
                        Parameter.string().key(CommandLiteral.X).terminal().build(),
                        Parameter.string().key(CommandLiteral.Z).terminal().build(),
                        Parameter.string().key(CommandLiteral.RADIUS_X).terminal().build(),
                        Parameter.string().key(CommandLiteral.RADIUS_Z).terminal().build()
                )
                .terminal(true)
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.TRIM);
                    ctx.one(Parameter.key(CommandLiteral.WORLD, ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.SHAPE, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.X, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.Z, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.RADIUS_X, String.class)).ifPresent(args::add);
                    ctx.one(Parameter.key(CommandLiteral.RADIUS_Z, String.class)).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.TRIM, args);
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized worldborderCommand = Command.builder()
                .permission("chunky.command.worldborder")
                .executor(ctx -> {
                    executeSpongeCommand(ctx, CommandLiteral.WORLDBORDER, Collections.singletonList(CommandLiteral.WORLDBORDER));
                    return CommandResult.success();
                })
                .build();
        Command.Parameterized worldCommand = Command.builder()
                .permission("chunky.command.world")
                .addParameters(Parameter.world().key(CommandLiteral.WORLD).build())
                .executor(ctx -> {
                    List<String> args = new ArrayList<>();
                    args.add(CommandLiteral.WORLD);
                    ctx.one(Parameter.key(CommandLiteral.WORLD, ServerWorld.class)).map(ServerWorld::key).map(ResourceKey::asString).ifPresent(args::add);
                    executeSpongeCommand(ctx, CommandLiteral.WORLD, args);
                    return CommandResult.success();
                })
                .build();
        event.register(this.container, Command.builder()
                .permission("chunky.command.base")
                .addChild(cancelCommand, CommandLiteral.CANCEL)
                .addChild(centerCommand, CommandLiteral.CENTER)
                .addChild(confirmCommand, CommandLiteral.CONFIRM)
                .addChild(continueCommand, CommandLiteral.CONTINUE)
                .addChild(cornersCommand, CommandLiteral.CORNERS)
                .addChild(helpCommand, CommandLiteral.HELP)
                .addChild(patternCommand, CommandLiteral.PATTERN)
                .addChild(pauseCommand, CommandLiteral.PAUSE)
                .addChild(progressCommand, CommandLiteral.PROGRESS)
                .addChild(quietCommand, CommandLiteral.QUIET)
                .addChild(radiusCommand, CommandLiteral.RADIUS)
                .addChild(reloadCommand, CommandLiteral.RELOAD)
                .addChild(shapeCommand, CommandLiteral.SHAPE)
                .addChild(silentCommand, CommandLiteral.SILENT)
                .addChild(spawnCommand, CommandLiteral.SPAWN)
                .addChild(startCommand, CommandLiteral.START)
                .addChild(trimCommand, CommandLiteral.TRIM)
                .addChild(worldborderCommand, CommandLiteral.WORLDBORDER)
                .addChild(worldCommand, CommandLiteral.WORLD)
                .executor(ctx -> {
                    executeSpongeCommand(ctx, CommandLiteral.HELP, Collections.emptyList());
                    return CommandResult.success();
                })
                .build(), CommandLiteral.CHUNKY);
    }

    private void executeSpongeCommand(final CommandContext ctx, final String command, final List<String> args) {
        final String[] argsArray = new String[args.size()];
        chunky.getCommands().get(command).execute(new SpongeSender(ctx.cause().root()), args.toArray(argsArray));
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
