package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.listeners.BossBarProgress;
import org.popcraft.chunky.platform.FabricSender;
import org.popcraft.chunky.platform.FabricServer;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.impl.GsonConfig;

import java.io.File;
import java.util.Map;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.DimensionArgumentType.dimension;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ChunkyFabric implements ModInitializer {
    private Chunky chunky;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "chunky.json");
            this.chunky = new Chunky(new FabricServer(this, minecraftServer), new GsonConfig(() -> chunky, configFile));
            if (chunky.getConfig().getContinueOnRestart()) {
                chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), new String[]{});
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> chunky.disable());
        ServerTickEvents.END_SERVER_TICK.register(server -> BossBarProgress.tick(chunky, server));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            Command<ServerCommandSource> command = context -> {
                Sender sender = new FabricSender(context.getSource());
                Map<String, ChunkyCommand> commands = chunky.getCommands();
                String input = context.getInput();
                int argsIndex = input.indexOf(' ');
                String[] args = input.substring(argsIndex < 0 ? 0 : argsIndex + 1).split(" ");
                String subCommand = args.length > 0 && commands.containsKey(args[0]) ? args[0] : CommandLiteral.HELP;
                commands.get(subCommand).execute(sender, args);
                return Command.SINGLE_SUCCESS;
            };
            dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal(CommandLiteral.CHUNKY)
                    .then(literal(CommandLiteral.CANCEL)
                            .then(argument(CommandLiteral.WORLD, dimension())
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.CENTER)
                            .then(argument(CommandLiteral.X, word())
                                    .then(argument(CommandLiteral.Z, word())
                                            .executes(command))
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.CONFIRM)
                            .executes(command))
                    .then(literal(CommandLiteral.CONTINUE)
                            .then(argument(CommandLiteral.WORLD, dimension())
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.CORNERS)
                            .then(argument(CommandLiteral.X1, word())
                                    .then(argument(CommandLiteral.Z1, word())
                                            .then(argument(CommandLiteral.X2, word())
                                                    .then(argument(CommandLiteral.Z2, word())
                                                            .executes(command))
                                                    .executes(command))
                                            .executes(command))
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.HELP)
                            .then(argument(CommandLiteral.PAGE, integer())
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.PATTERN)
                            .then(argument(CommandLiteral.PATTERN, string())
                                    .suggests(SuggestionProviders.PATTERNS)
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.PAUSE)
                            .then(argument(CommandLiteral.WORLD, dimension())
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.PROGRESS)
                            .executes(command))
                    .then(literal(CommandLiteral.QUIET)
                            .then(argument(CommandLiteral.INTERVAL, integer())
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.RADIUS)
                            .then(argument(CommandLiteral.RADIUS, word())
                                    .then(argument(CommandLiteral.RADIUS, word())
                                            .executes(command))
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.RELOAD)
                            .executes(command))
                    .then(literal(CommandLiteral.SHAPE)
                            .then(argument(CommandLiteral.SHAPE, string())
                                    .suggests(SuggestionProviders.SHAPES)
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.SILENT)
                            .executes(command))
                    .then(literal(CommandLiteral.SPAWN)
                            .executes(command))
                    .then(literal(CommandLiteral.START)
                            .then(argument(CommandLiteral.WORLD, dimension())
                                    .then(argument(CommandLiteral.SHAPE, string())
                                            .then(argument(CommandLiteral.CENTER_X, word())
                                                    .then(argument(CommandLiteral.CENTER_Z, word())
                                                            .then(argument(CommandLiteral.RADIUS_X, word())
                                                                    .then(argument(CommandLiteral.RADIUS_Z, word())
                                                                            .executes(command))
                                                                    .executes(command))
                                                            .executes(command))
                                                    .executes(command))
                                            .suggests(SuggestionProviders.SHAPES)
                                            .executes(command))
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.TRIM)
                            .then(argument(CommandLiteral.WORLD, dimension())
                                    .then(argument(CommandLiteral.SHAPE, string())
                                            .then(argument(CommandLiteral.CENTER_X, word())
                                                    .then(argument(CommandLiteral.CENTER_Z, word())
                                                            .then(argument(CommandLiteral.RADIUS_X, word())
                                                                    .then(argument(CommandLiteral.RADIUS_Z, word())
                                                                            .executes(command))
                                                                    .executes(command))
                                                            .executes(command))
                                                    .executes(command))
                                            .suggests(SuggestionProviders.SHAPES)
                                            .executes(command))
                                    .executes(command))
                            .executes(command))
                    .then(literal(CommandLiteral.WORLDBORDER)
                            .executes(command))
                    .then(literal(CommandLiteral.WORLD)
                            .then(argument(CommandLiteral.WORLD, dimension())
                                    .executes(command))
                            .executes(command))
                    .executes(command)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)));
        });
    }

    public Chunky getChunky() {
        return chunky;
    }
}
