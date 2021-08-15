package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.popcraft.chunky.command.ChunkyCommand;
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
            chunky.getCommands().get("continue").execute(chunky.getServer().getConsoleSender(), new String[]{});
        }
        Command<CommandSourceStack> command = context -> {
            Sender sender = new ForgeSender(context.getSource());
            Map<String, ChunkyCommand> commands = chunky.getCommands();
            String input = context.getInput();
            int argsIndex = input.indexOf(' ');
            String[] args = input.substring(argsIndex < 0 ? 0 : argsIndex + 1).split(" ");
            String subCommand = args.length > 0 && commands.containsKey(args[0]) ? args[0] : "help";
            commands.get(subCommand).execute(sender, args);
            return Command.SINGLE_SUCCESS;
        };
        server.getCommands().getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal(MODID)
                .then(literal("cancel")
                        .then(argument("world", dimension())
                                .executes(command))
                        .executes(command))
                .then(literal("center")
                        .then(argument("x", word())
                                .then(argument("z", word())
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("confirm")
                        .executes(command))
                .then(literal("continue")
                        .then(argument("world", dimension())
                                .executes(command))
                        .executes(command))
                .then(literal("corners")
                        .then(argument("x1", word())
                                .then(argument("z1", word())
                                        .then(argument("x2", word())
                                                .then(argument("z2", word())
                                                        .executes(command))
                                                .executes(command))
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("help")
                        .then(argument("page", integer())
                                .executes(command))
                        .executes(command))
                .then(literal("pattern")
                        .then(argument("pattern", string())
                                .suggests(SuggestionProviders.PATTERNS)
                                .executes(command))
                        .executes(command))
                .then(literal("pause")
                        .then(argument("world", dimension())
                                .executes(command))
                        .executes(command))
                .then(literal("progress")
                        .executes(command))
                .then(literal("quiet")
                        .then(argument("interval", integer())
                                .executes(command))
                        .executes(command))
                .then(literal("radius")
                        .then(argument("radius", word())
                                .then(argument("radius", word())
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("reload")
                        .executes(command))
                .then(literal("shape")
                        .then(argument("shape", string())
                                .suggests(SuggestionProviders.SHAPES)
                                .executes(command))
                        .executes(command))
                .then(literal("silent")
                        .executes(command))
                .then(literal("spawn")
                        .executes(command))
                .then(literal("start")
                        .then(argument("world", dimension())
                                .then(argument("shape", string())
                                        .then(argument("centerX", word())
                                                .then(argument("centerZ", word())
                                                        .then(argument("radiusX", word())
                                                                .then(argument("radiusZ", word())
                                                                        .executes(command))
                                                                .executes(command))
                                                        .executes(command))
                                                .executes(command))
                                        .suggests(SuggestionProviders.SHAPES)
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("trim")
                        .then(argument("world", dimension())
                                .then(argument("shape", string())
                                        .then(argument("centerX", word())
                                                .then(argument("centerZ", word())
                                                        .then(argument("radiusX", word())
                                                                .then(argument("radiusZ", word())
                                                                        .executes(command))
                                                                .executes(command))
                                                        .executes(command))
                                                .executes(command))
                                        .suggests(SuggestionProviders.SHAPES)
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("worldborder")
                        .executes(command))
                .then(literal("world")
                        .then(argument("world", dimension())
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
