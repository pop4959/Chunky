package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
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
import java.util.Arrays;
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
            final LiteralArgumentBuilder<ServerCommandSource> command = literal(CommandLiteral.CHUNKY)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .executes(context -> {
                        Sender sender = new FabricSender(context.getSource());
                        Map<String, ChunkyCommand> commands = chunky.getCommands();
                        String input = context.getInput().substring(context.getLastChild().getNodes().get(0).getRange().getStart());
                        String[] tokens = input.split(" ");
                        String subCommand = tokens.length > 1 && commands.containsKey(tokens[1]) ? tokens[1] : CommandLiteral.HELP;
                        String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
                        commands.get(subCommand).execute(sender, args);
                        return Command.SINGLE_SUCCESS;
                    });
            registerArguments(command, literal(CommandLiteral.CANCEL),
                    argument(CommandLiteral.WORLD, dimension()));
            registerArguments(command, literal(CommandLiteral.CENTER),
                    argument(CommandLiteral.X, word()),
                    argument(CommandLiteral.Z, word()));
            registerArguments(command, literal(CommandLiteral.CONFIRM));
            registerArguments(command, literal(CommandLiteral.CONTINUE),
                    argument(CommandLiteral.WORLD, dimension()));
            registerArguments(command, literal(CommandLiteral.CORNERS),
                    argument(CommandLiteral.X1, word()),
                    argument(CommandLiteral.Z1, word()),
                    argument(CommandLiteral.X2, word()),
                    argument(CommandLiteral.Z2, word()));
            registerArguments(command, literal(CommandLiteral.HELP),
                    argument(CommandLiteral.PAGE, integer()));
            registerArguments(command, literal(CommandLiteral.PATTERN),
                    argument(CommandLiteral.PATTERN, string()).suggests(SuggestionProviders.PATTERNS));
            registerArguments(command, literal(CommandLiteral.PAUSE),
                    argument(CommandLiteral.WORLD, dimension()));
            registerArguments(command, literal(CommandLiteral.PROGRESS));
            registerArguments(command, literal(CommandLiteral.QUIET),
                    argument(CommandLiteral.INTERVAL, integer()));
            registerArguments(command, literal(CommandLiteral.RADIUS),
                    argument(CommandLiteral.RADIUS, word()),
                    argument(CommandLiteral.RADIUS, word()));
            registerArguments(command, literal(CommandLiteral.RELOAD));
            registerArguments(command, literal(CommandLiteral.SHAPE),
                    argument(CommandLiteral.SHAPE, string()).suggests(SuggestionProviders.SHAPES));
            registerArguments(command, literal(CommandLiteral.SILENT));
            registerArguments(command, literal(CommandLiteral.SPAWN));
            registerArguments(command, literal(CommandLiteral.START),
                    argument(CommandLiteral.WORLD, dimension()),
                    argument(CommandLiteral.SHAPE, string()).suggests(SuggestionProviders.SHAPES),
                    argument(CommandLiteral.CENTER_X, word()),
                    argument(CommandLiteral.CENTER_Z, word()),
                    argument(CommandLiteral.RADIUS_X, word()),
                    argument(CommandLiteral.RADIUS_Z, word()));
            registerArguments(command, literal(CommandLiteral.TRIM),
                    argument(CommandLiteral.WORLD, dimension()),
                    argument(CommandLiteral.SHAPE, string()).suggests(SuggestionProviders.SHAPES),
                    argument(CommandLiteral.CENTER_X, word()),
                    argument(CommandLiteral.CENTER_Z, word()),
                    argument(CommandLiteral.RADIUS_X, word()),
                    argument(CommandLiteral.RADIUS_Z, word()));
            registerArguments(command, literal(CommandLiteral.WORLDBORDER));
            registerArguments(command, literal(CommandLiteral.WORLD),
                    argument(CommandLiteral.WORLD, dimension()));
            dispatcher.register(command);
        });
    }

    @SafeVarargs
    private <S> void registerArguments(final LiteralArgumentBuilder<S> command, final ArgumentBuilder<S, ?>... arguments) {
        for (int i = arguments.length - 1; i > 0; --i) {
            arguments[i - 1].then(arguments[i].executes(command.getCommand()));
        }
        command.then(arguments[0].executes(command.getCommand()));
    }

    public Chunky getChunky() {
        return chunky;
    }
}
