package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskFinishListener;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskUpdateListener;
import org.popcraft.chunky.platform.FabricPlayer;
import org.popcraft.chunky.platform.FabricSender;
import org.popcraft.chunky.platform.FabricServer;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.impl.GsonConfig;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.DimensionArgument.dimension;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class ChunkyFabric implements ModInitializer {
    public static final boolean ENABLE_MOONRISE_WORKAROUNDS = FabricLoader.getInstance().isModLoaded("moonrise");
    private Chunky chunky;
    private final Map<ResourceLocation, ServerBossEvent> bossBars = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("chunky/config.json");
            this.chunky = new Chunky(new FabricServer(this, minecraftServer), new GsonConfig(configPath));
            if (chunky.getConfig().getContinueOnRestart()) {
                chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), CommandArguments.empty());
            }
            chunky.getEventBus().subscribe(GenerationTaskUpdateEvent.class, new BossBarTaskUpdateListener(bossBars));
            chunky.getEventBus().subscribe(GenerationTaskFinishEvent.class, new BossBarTaskFinishListener(bossBars));
            FabricLoader.getInstance().getEntrypointContainers("chunky", ModInitializer.class)
                    .forEach(entryPoint -> entryPoint.getEntrypoint().onInitialize());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            if (chunky != null) {
                chunky.disable();
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            final LiteralArgumentBuilder<CommandSourceStack> command = literal(CommandLiteral.CHUNKY)
                    .requires(serverCommandSource -> {
                        final MinecraftServer minecraftServer = serverCommandSource.getServer();
                        if (minecraftServer != null && minecraftServer.isSingleplayer()) {
                            return true;
                        }
                        return new FabricSender(serverCommandSource).hasPermission("chunky.command", true);
                    })
                    .executes(context -> {
                        final Sender sender;
                        if (context.getSource().getEntity() instanceof final ServerPlayer player) {
                            sender = new FabricPlayer(player);
                        } else {
                            sender = new FabricSender(context.getSource());
                        }
                        final Map<String, ChunkyCommand> commands = chunky.getCommands();
                        final String input = context.getInput().substring(context.getLastChild().getNodes().get(0).getRange().getStart());
                        final String[] tokens = input.split(" ");
                        final String subCommand = tokens.length > 1 && commands.containsKey(tokens[1]) ? tokens[1] : CommandLiteral.HELP;
                        final CommandArguments arguments = tokens.length > 2 ? CommandArguments.of(Arrays.copyOfRange(tokens, 2, tokens.length)) : CommandArguments.empty();
                        commands.get(subCommand).execute(sender, arguments);
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
                    argument(CommandLiteral.PATTERN, string()).suggests(SuggestionProviders.PATTERNS),
                    argument(CommandLiteral.VALUE, string()));
            registerArguments(command, literal(CommandLiteral.PAUSE),
                    argument(CommandLiteral.WORLD, dimension()));
            registerArguments(command, literal(CommandLiteral.PROGRESS));
            registerArguments(command, literal(CommandLiteral.QUIET),
                    argument(CommandLiteral.INTERVAL, integer()));
            registerArguments(command, literal(CommandLiteral.RADIUS),
                    argument(CommandLiteral.RADIUS, word()),
                    argument(CommandLiteral.RADIUS, word()));
            registerArguments(command, literal(CommandLiteral.RELOAD),
                    argument(CommandLiteral.TYPE, word()));
            registerArguments(command, literal(CommandLiteral.SELECTION));
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
                    argument(CommandLiteral.RADIUS_Z, word()),
                    argument(CommandLiteral.TRIM_MODE, string()).suggests(SuggestionProviders.TRIM_MODES),
                    argument(CommandLiteral.INHABITED, word()));
            registerArguments(command, literal(CommandLiteral.WORLDBORDER));
            registerArguments(command, literal(CommandLiteral.WORLD),
                    argument(CommandLiteral.WORLD, dimension()));
            final LiteralArgumentBuilder<CommandSourceStack> borderCommand = literal(CommandLiteral.BORDER)
                    .requires(serverCommandSource -> chunky != null && chunky.getCommands().containsKey(CommandLiteral.BORDER))
                    .executes(command.getCommand());
            registerArguments(borderCommand, literal(CommandLiteral.ADD),
                    argument(CommandLiteral.WORLD, dimension()),
                    argument(CommandLiteral.SHAPE, string()).suggests(SuggestionProviders.SHAPES),
                    argument(CommandLiteral.CENTER_X, word()),
                    argument(CommandLiteral.CENTER_Z, word()),
                    argument(CommandLiteral.RADIUS_X, word()),
                    argument(CommandLiteral.RADIUS_Z, word()));
            registerArguments(borderCommand, literal(CommandLiteral.BYPASS),
                    argument(CommandLiteral.PLAYER, player()));
            registerArguments(borderCommand, literal(CommandLiteral.HELP));
            registerArguments(borderCommand, literal(CommandLiteral.LIST));
            registerArguments(borderCommand, literal(CommandLiteral.LOAD),
                    argument(CommandLiteral.WORLD, dimension()));
            registerArguments(borderCommand, literal(CommandLiteral.REMOVE),
                    argument(CommandLiteral.WORLD, dimension()));
            registerArguments(borderCommand, literal(CommandLiteral.WRAP),
                    argument(CommandLiteral.WRAP, word()));
            registerArguments(command, borderCommand);
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
