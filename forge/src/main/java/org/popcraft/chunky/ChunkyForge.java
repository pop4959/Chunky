package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskFinishListener;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskUpdateListener;
import org.popcraft.chunky.platform.ForgeSender;
import org.popcraft.chunky.platform.ForgeServer;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.impl.GsonConfig;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.DimensionArgument.dimension;

@Mod(ChunkyForge.MODID)
public class ChunkyForge {
    public static final String MODID = "chunky";
    private Chunky chunky;

    public ChunkyForge() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        final MinecraftServer server = event.getServer();
        final Path configPath = event.getServer().getServerDirectory().toPath().resolve("config/chunky.json");
        this.chunky = new Chunky(new ForgeServer(this, server), new GsonConfig(() -> chunky, configPath));
        if (chunky.getConfig().getContinueOnRestart()) {
            chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), new String[]{});
        }
        chunky.getEventBus().subscribe(GenerationTaskUpdateEvent.class, new BossBarTaskUpdateListener());
        chunky.getEventBus().subscribe(GenerationTaskFinishEvent.class, new BossBarTaskFinishListener());
        final LiteralArgumentBuilder<CommandSourceStack> command = literal(CommandLiteral.CHUNKY)
                .requires(serverCommandSource -> serverCommandSource.hasPermission(2))
                .executes(context -> {
                    final Sender sender = new ForgeSender(context.getSource());
                    final Map<String, ChunkyCommand> commands = chunky.getCommands();
                    final String input = context.getInput().substring(context.getLastChild().getNodes().get(0).getRange().getStart());
                    final String[] tokens = input.split(" ");
                    final String subCommand = tokens.length > 1 && commands.containsKey(tokens[1]) ? tokens[1] : CommandLiteral.HELP;
                    final String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[]{};
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
        server.getCommands().getDispatcher().register(command);
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
