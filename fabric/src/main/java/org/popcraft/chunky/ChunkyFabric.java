package org.popcraft.chunky;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.platform.FabricConfig;
import org.popcraft.chunky.platform.FabricPlatform;
import org.popcraft.chunky.platform.FabricSender;
import org.popcraft.chunky.platform.Sender;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.DimensionArgumentType.dimension;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ChunkyFabric implements ModInitializer {
    private Chunky chunky;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            this.chunky = new Chunky(new FabricPlatform(this, minecraftServer));
            chunky.setConfig(new FabricConfig(chunky));
            Optional<FabricConfig.ConfigModel> configModel = ((FabricConfig) chunky.getConfig()).getConfigModel();
            InputStream configLanguage = getClass().getClassLoader().getResourceAsStream("lang/" + configModel.map(model -> model.language).orElse("en") + ".json");
            InputStream defaultLanguage = getClass().getClassLoader().getResourceAsStream("lang/en.json");
            if (configLanguage == null) {
                configLanguage = defaultLanguage;
            }
            chunky.setTranslations(chunky.loadTranslation(configLanguage));
            chunky.setFallbackTranslations(chunky.loadTranslation(defaultLanguage));
            chunky.loadCommands();
            if (configModel.isPresent() && configModel.get().continueOnRestart) {
                chunky.getCommands().get("continue").execute(chunky.getPlatform().getServer().getConsoleSender(), new String[]{});
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            chunky.getConfig().saveTasks();
            chunky.getGenerationTasks().values().forEach(generationTask -> generationTask.stop(false));
            chunky.getPlatform().getServer().getScheduler().cancelTasks();
        });
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
            SuggestionProvider<ServerCommandSource> shapeSuggestionProvider = (commandContext, suggestionsBuilder) -> {
                List<String> suggestions = chunky.getCommands().get("shape").tabSuggestions(new FabricSender(commandContext.getSource()), new String[]{});
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
            dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("chunky")
                    .then(literal("cancel")
                            .then(argument("world", dimension())
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
                            .then(argument("world", dimension())
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
                                        List<String> suggestions = chunky.getCommands().get("pattern").tabSuggestions(new FabricSender(commandContext.getSource()), new String[]{});
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
                            .then(argument("world", dimension())
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
                            .then(argument("world", dimension())
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
