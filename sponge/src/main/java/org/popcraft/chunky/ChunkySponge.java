package org.popcraft.chunky;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.platform.SpongeConfig;
import org.popcraft.chunky.platform.SpongePlatform;
import org.popcraft.chunky.platform.SpongeSender;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Plugin(
        id = "chunky",
        name = "Chunky",
        version = "1.0-SNAPSHOT",
        description = "Pre-generates chunks, quickly, efficiently, and safely",
        url = "https://github.com/pop4959/Chunky",
        authors = {
                "pop4959"
        }
)
public class ChunkySponge {
    private Chunky chunky;
    @Inject
    private Logger logger;
    @Inject
    private Game game;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configPath;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        this.chunky = new Chunky(new SpongePlatform(this));
        chunky.setConfig(new SpongeConfig(this));
        InputStream configLanguage = getClass().getClassLoader().getResourceAsStream("lang/en.json");
        InputStream defaultLanguage = getClass().getClassLoader().getResourceAsStream("lang/en.json");
        if (configLanguage == null) {
            configLanguage = defaultLanguage;
        }
        chunky.setTranslations(chunky.loadTranslation(configLanguage));
        chunky.setFallbackTranslations(chunky.loadTranslation(defaultLanguage));
        chunky.loadCommands();
        CommandSpec cancelCommand = CommandSpec.builder()
                .permission("chunky.command.cancel")
                .executor(noArgsCommand("cancel"))
                .build();
        CommandSpec centerCommand = CommandSpec.builder()
                .permission("chunky.command.center")
                .arguments(
                        GenericArguments.integer(Text.of("x")),
                        GenericArguments.integer(Text.of("z")))
                .executor((CommandSource source, CommandContext context) -> {
                    ChunkyCommand cmd = chunky.getCommands().get("center");
                    cmd.execute(new SpongeSender(source), new String[]{
                            "center",
                            context.<Integer>getOne(Text.of("x")).orElse(0).toString(),
                            context.<Integer>getOne(Text.of("z")).orElse(0).toString()
                    });
                    return CommandResult.success();
                })
                .build();
        CommandSpec confirmCommand = CommandSpec.builder()
                .permission("chunky.command.confirm")
                .executor(noArgsCommand("confirm"))
                .build();
        CommandSpec continueCommand = CommandSpec.builder()
                .permission("chunky.command.continue")
                .executor(noArgsCommand("continue"))
                .build();
        CommandSpec cornersCommand = CommandSpec.builder()
                .permission("chunky.command.corners")
                .executor(noArgsCommand("corners"))
                .arguments(
                        GenericArguments.integer(Text.of("x1")),
                        GenericArguments.integer(Text.of("z1")),
                        GenericArguments.integer(Text.of("x2")),
                        GenericArguments.integer(Text.of("z2")))
                .executor((CommandSource source, CommandContext context) -> {
                    ChunkyCommand cmd = chunky.getCommands().get("corners");
                    cmd.execute(new SpongeSender(source), new String[]{
                            "corners",
                            context.<Integer>getOne(Text.of("x1")).orElse(0).toString(),
                            context.<Integer>getOne(Text.of("z1")).orElse(0).toString(),
                            context.<Integer>getOne(Text.of("x2")).orElse(0).toString(),
                            context.<Integer>getOne(Text.of("z2")).orElse(0).toString()
                    });
                    return CommandResult.success();
                })
                .build();
        CommandSpec helpCommand = CommandSpec.builder()
                .permission("chunky.command.help")
                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("page"))))
                .executor((CommandSource source, CommandContext context) -> {
                    ChunkyCommand cmd = chunky.getCommands().get("help");
                    cmd.execute(new SpongeSender(source), new String[]{
                            "help",
                            context.<Integer>getOne(Text.of("page")).orElse(0).toString()
                    });
                    return CommandResult.success();
                })
                .build();
        CommandSpec patternCommand = CommandSpec.builder()
                .permission("chunky.command.pattern")
                .arguments(GenericArguments.string(Text.of("pattern")))
                .executor((CommandSource source, CommandContext context) -> {
                    ChunkyCommand cmd = chunky.getCommands().get("pattern");
                    cmd.execute(new SpongeSender(source), new String[]{
                            "pattern",
                            context.<String>getOne(Text.of("pattern")).orElse("concentric")
                    });
                    return CommandResult.success();
                })
                .build();
        CommandSpec pauseCommand = CommandSpec.builder()
                .permission("chunky.command.pause")
                .executor(noArgsCommand("pause"))
                .build();
        CommandSpec quietCommand = CommandSpec.builder()
                .permission("chunky.command.quiet")
                .arguments(GenericArguments.integer(Text.of("interval")))
                .executor((CommandSource source, CommandContext context) -> {
                    ChunkyCommand cmd = chunky.getCommands().get("quiet");
                    cmd.execute(new SpongeSender(source), new String[]{
                            "quiet",
                            context.<Integer>getOne(Text.of("interval")).orElse(1).toString()
                    });
                    return CommandResult.success();
                })
                .build();
        CommandSpec radiusCommand = CommandSpec.builder()
                .permission("chunky.command.radius")
                .arguments(
                        GenericArguments.integer(Text.of("radius")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("radius"))))
                .executor((CommandSource source, CommandContext context) -> {
                    ChunkyCommand cmd = chunky.getCommands().get("radius");
                    final List<String> args = new ArrayList<>(Collections.singletonList("radius"));
                    context.<Integer>getAll(Text.of("radius")).forEach(r -> args.add(String.valueOf(r)));
                    cmd.execute(new SpongeSender(source), args.toArray(new String[0]));
                    return CommandResult.success();
                })
                .build();
        CommandSpec reloadCommand = CommandSpec.builder()
                .permission("chunky.command.reload")
                .executor(noArgsCommand("reload"))
                .build();
        CommandSpec shapeCommand = CommandSpec.builder()
                .permission("chunky.command.shape")
                .arguments(GenericArguments.string(Text.of("shape")))
                .executor((CommandSource source, CommandContext context) -> {
                    ChunkyCommand cmd = chunky.getCommands().get("shape");
                    cmd.execute(new SpongeSender(source), new String[]{
                            "shape",
                            context.<String>getOne(Text.of("shape")).orElse("square")
                    });
                    return CommandResult.success();
                })
                .build();
        CommandSpec silentCommand = CommandSpec.builder()
                .permission("chunky.command.silent")
                .executor(noArgsCommand("silent"))
                .build();
        CommandSpec spawnCommand = CommandSpec.builder()
                .permission("chunky.command.spawn")
                .executor(noArgsCommand("spawn"))
                .build();
        CommandSpec startCommand = CommandSpec.builder()
                .permission("chunky.command.start")
                .executor(noArgsCommand("start"))
                .build();
        CommandSpec worldborderCommand = CommandSpec.builder()
                .permission("chunky.command.worldborder")
                .executor(noArgsCommand("worldborder"))
                .build();
        CommandSpec worldCommand = CommandSpec.builder()
                .permission("chunky.command.world")
                .arguments(GenericArguments.world(Text.of("world")))
                .executor((CommandSource source, CommandContext context) -> {
                    ChunkyCommand cmd = chunky.getCommands().get("world");
                    cmd.execute(new SpongeSender(source), new String[]{
                            "world",
                            context.<WorldProperties>getOne(Text.of("world"))
                                    .map(WorldProperties::getWorldName)
                                    .orElse(Sponge.getServer().getDefaultWorldName())
                    });
                    return CommandResult.success();
                })
                .build();
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .permission("chunky.command.base")
                .child(cancelCommand, "cancel")
                .child(centerCommand, "center")
                .child(confirmCommand, "confirm")
                .child(continueCommand, "continue")
                .child(cornersCommand, "corners")
                .child(helpCommand, "help")
                .child(patternCommand, "pattern")
                .child(pauseCommand, "pause")
                .child(quietCommand, "quiet")
                .child(radiusCommand, "radius")
                .child(reloadCommand, "reload")
                .child(shapeCommand, "shape")
                .child(silentCommand, "silent")
                .child(spawnCommand, "spawn")
                .child(startCommand, "start")
                .child(worldborderCommand, "worldborder")
                .child(worldCommand, "world")
                .executor(noArgsCommand("help"))
                .build(), "chunky");
    }

    private CommandExecutor noArgsCommand(final String name) {
        return (CommandSource source, CommandContext context) -> {
            ChunkyCommand cmd = chunky.getCommands().get(name);
            cmd.execute(new SpongeSender(source), new String[]{name});
            return CommandResult.success();
        };
    }

    public Chunky getChunky() {
        return chunky;
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

    public ConfigurationLoader<CommentedConfigurationNode> getConfigManager() {
        return configManager;
    }
}
