package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.listeners.BossBarProgress;
import org.popcraft.chunky.platform.ForgeSender;
import org.popcraft.chunky.platform.ForgeServer;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.impl.GsonConfig;

import java.io.File;
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
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        File configFile = new File(event.getServer().getServerDirectory(), "config/chunky.json");
        this.chunky = new Chunky(new ForgeServer(this, server), new GsonConfig(() -> chunky, configFile));
        if (chunky.getConfig().getContinueOnRestart()) {
            chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), new String[]{});
        }
        Command<CommandSourceStack> command = context -> {
            Sender sender = new ForgeSender(context.getSource());
            Map<String, ChunkyCommand> commands = chunky.getCommands();
            String input = context.getInput();
            int argsIndex = input.indexOf(' ');
            String[] args = input.substring(argsIndex < 0 ? 0 : argsIndex + 1).split(" ");
            String subCommand = args.length > 0 && commands.containsKey(args[0]) ? args[0] : CommandLiteral.HELP;
            commands.get(subCommand).execute(sender, args);
            return Command.SINGLE_SUCCESS;
        };
        server.getCommands().getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal(CommandLiteral.CHUNKY)
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
                .requires(serverCommandSource -> serverCommandSource.hasPermission(2)));
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        chunky.disable();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                BossBarProgress.tick(chunky, server);
            }
        }
    }

    public Chunky getChunky() {
        return chunky;
    }
}
