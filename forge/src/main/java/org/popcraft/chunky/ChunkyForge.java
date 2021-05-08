package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.platform.ForgePlatform;
import org.popcraft.chunky.platform.ForgeSender;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.impl.GsonConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.arguments.DimensionArgument.getDimension;

@Mod(ChunkyForge.MODID)
public class ChunkyForge {
    public static final String MODID = "chunky";
    private static final Logger LOGGER = LogManager.getLogger();
    private Chunky chunky;

    public ChunkyForge() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        this.chunky = new Chunky(new ForgePlatform(this, server));
        File configFile = new File(event.getServer().getDataDirectory(), "config/chunky.json");
        chunky.setConfig(new GsonConfig(chunky, configFile));
        chunky.setLanguage(chunky.getConfig().getLanguage());
        chunky.loadCommands();
        if (chunky.getConfig().getContinueOnRestart()) {
            chunky.getCommands().get("continue").execute(chunky.getPlatform().getServer().getConsoleSender(), new String[]{});
        }
        Command<CommandSource> command = context -> {
            Sender sender = new ForgeSender(context.getSource());
            Map<String, ChunkyCommand> commands = chunky.getCommands();
            String input = context.getInput();
            int argsIndex = input.indexOf(' ');
            String[] args = input.substring(argsIndex < 0 ? 0 : argsIndex + 1).split(" ");
            String subCommand = args.length > 0 && commands.containsKey(args[0]) ? args[0] : "help";
            commands.get(subCommand).execute(sender, args);
            return Command.SINGLE_SUCCESS;
        };
        SuggestionProvider<CommandSource> shapeSuggestionProvider = (commandContext, suggestionsBuilder) -> {
            List<String> suggestions = chunky.getCommands().get("shape").tabSuggestions(new ForgeSender(commandContext.getSource()), new String[]{});
            try {
                final String arg = commandContext.getArgument("shape", String.class);
                suggestions.stream()
                        .filter(s -> arg == null || s.toLowerCase().startsWith(arg.toLowerCase()))
                        .forEach(suggestionsBuilder::suggest);
            } catch (IllegalArgumentException ignored) {
                suggestions.forEach(suggestionsBuilder::suggest);
            }
            return suggestionsBuilder.buildFuture();
        };
        server.getCommandManager().getDispatcher().register(LiteralArgumentBuilder.<CommandSource>literal("chunky")
                .then(literal("cancel")
                        .then(argument("world", getDimension())
                                .executes(command))
                        .executes(command))
                .then(literal("center")
                        .then(argument("x", integer())
                                .then(argument("z", integer())
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("confirm")
                        .executes(command))
                .then(literal("continue")
                        .then(argument("world", getDimension())
                                .executes(command))
                        .executes(command))
                .then(literal("corners")
                        .then(argument("x1", integer())
                                .then(argument("z1", integer())
                                        .then(argument("x2", integer())
                                                .then(argument("z2", integer())
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
                                .suggests((commandContext, suggestionsBuilder) -> {
                                    List<String> suggestions = chunky.getCommands().get("pattern").tabSuggestions(new ForgeSender(commandContext.getSource()), new String[]{});
                                    try {
                                        final String arg = commandContext.getArgument("pattern", String.class);
                                        suggestions.stream()
                                                .filter(s -> arg == null || s.toLowerCase().startsWith(arg.toLowerCase()))
                                                .forEach(suggestionsBuilder::suggest);
                                    } catch (IllegalArgumentException ignored) {
                                        suggestions.forEach(suggestionsBuilder::suggest);
                                    }
                                    return suggestionsBuilder.buildFuture();
                                })
                                .executes(command))
                        .executes(command))
                .then(literal("pause")
                        .then(argument("world", getDimension())
                                .executes(command))
                        .executes(command))
                .then(literal("quiet")
                        .then(argument("interval", integer())
                                .executes(command))
                        .executes(command))
                .then(literal("radius")
                        .then(argument("radius", integer())
                                .then(argument("radius", integer())
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("reload")
                        .executes(command))
                .then(literal("shape")
                        .then(argument("shape", string())
                                .suggests(shapeSuggestionProvider)
                                .executes(command))
                        .executes(command))
                .then(literal("silent")
                        .executes(command))
                .then(literal("spawn")
                        .executes(command))
                .then(literal("start")
                        .then(argument("world", getDimension())
                                .then(argument("shape", string())
                                        .then(argument("centerX", integer())
                                                .then(argument("centerZ", integer())
                                                        .then(argument("radiusX", integer())
                                                                .then(argument("radiusZ", integer())
                                                                        .executes(command))
                                                                .executes(command))
                                                        .executes(command))
                                                .executes(command))
                                        .suggests(shapeSuggestionProvider)
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("trim")
                        .then(argument("world", getDimension())
                                .then(argument("shape", string())
                                        .then(argument("centerX", integer())
                                                .then(argument("centerZ", integer())
                                                        .then(argument("radiusX", integer())
                                                                .then(argument("radiusZ", integer())
                                                                        .executes(command))
                                                                .executes(command))
                                                        .executes(command))
                                                .executes(command))
                                        .suggests(shapeSuggestionProvider)
                                        .executes(command))
                                .executes(command))
                        .executes(command))
                .then(literal("worldborder")
                        .executes(command))
                .then(literal("world")
                        .then(argument("world", getDimension())
                                .executes(command))
                        .executes(command))
                .executes(command)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)));
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        chunky.getConfig().saveTasks();
        chunky.getGenerationTasks().values().forEach(generationTask -> generationTask.stop(false));
        chunky.getPlatform().getServer().getScheduler().cancelTasks();
    }

    public Chunky getChunky() {
        return chunky;
    }
}
