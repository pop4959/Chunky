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
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.listeners.BossBarProgress;
import org.popcraft.chunky.platform.FabricSender;
import org.popcraft.chunky.platform.FabricServer;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.impl.GsonConfig;
import org.popcraft.chunky.util.Limit;

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
            this.chunky = new Chunky(new FabricServer(this, minecraftServer));
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "chunky.json");
            chunky.setConfig(new GsonConfig(chunky, configFile));
            chunky.setLanguage(chunky.getConfig().getLanguage());
            chunky.loadCommands();
            Limit.set(chunky.getConfig());
            if (chunky.getConfig().getContinueOnRestart()) {
                chunky.getCommands().get("continue").execute(chunky.getServer().getConsoleSender(), new String[]{});
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
                String subCommand = args.length > 0 && commands.containsKey(args[0]) ? args[0] : "help";
                commands.get(subCommand).execute(sender, args);
                return Command.SINGLE_SUCCESS;
            };
            dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("chunky")
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
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)));
        });
    }

    public Chunky getChunky() {
        return chunky;
    }
}
