package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskFinishListener;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskUpdateListener;
import org.popcraft.chunky.platform.NeoForgePlayer;
import org.popcraft.chunky.platform.NeoForgeSender;
import org.popcraft.chunky.platform.NeoForgeServer;
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

@Mod(ChunkyNeoForge.MOD_ID)
public class ChunkyNeoForge {
    public static final String MOD_ID = "chunky";
    private Chunky chunky;
    private final Map<ResourceLocation, ServerBossEvent> bossBars = new ConcurrentHashMap<>();

    public ChunkyNeoForge() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        final MinecraftServer server = event.getServer();
        final Path configPath = FMLPaths.CONFIGDIR.get().resolve("chunky/config.json");
        this.chunky = new Chunky(new NeoForgeServer(this, server), new GsonConfig(configPath));
        if (chunky.getConfig().getContinueOnRestart()) {
            chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), CommandArguments.empty());
        }
        chunky.getEventBus().subscribe(GenerationTaskUpdateEvent.class, new BossBarTaskUpdateListener(bossBars));
        chunky.getEventBus().subscribe(GenerationTaskFinishEvent.class, new BossBarTaskFinishListener(bossBars));
    }

    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        final LiteralArgumentBuilder<CommandSourceStack> command = literal(CommandLiteral.CHUNKY)
                .requires(serverCommandSource -> {
                    if (serverCommandSource.getServer().isSingleplayer()) {
                        return true;
                    }
                    return serverCommandSource.hasPermission(2);
                })
                .executes(context -> {
                    final Sender sender;
                    if (context.getSource().getEntity() instanceof final ServerPlayer player) {
                        sender = new NeoForgePlayer(player);
                    } else {
                        sender = new NeoForgeSender(context.getSource());
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
        event.getDispatcher().register(command);
    }

    @SafeVarargs
    private <S> void registerArguments(final LiteralArgumentBuilder<S> command, final ArgumentBuilder<S, ?>... arguments) {
        for (int i = arguments.length - 1; i > 0; --i) {
            arguments[i - 1].then(arguments[i].executes(command.getCommand()));
        }
        command.then(arguments[0].executes(command.getCommand()));
    }

    @SubscribeEvent
    public void onServerStopping(final ServerStoppingEvent event) {
        if (chunky != null) {
            chunky.disable();
        }
    }

    public Chunky getChunky() {
        return chunky;
    }
}
