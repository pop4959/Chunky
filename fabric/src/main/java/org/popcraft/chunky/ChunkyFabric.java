package org.popcraft.chunky;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.commands.brigadier.FabricChunkyCommand;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskFinishListener;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskUpdateListener;
import org.popcraft.chunky.platform.FabricServer;
import org.popcraft.chunky.platform.impl.GsonConfig;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkyFabric implements ModInitializer {
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
            FabricChunkyCommand command = new FabricChunkyCommand(chunky);
            dispatcher.register(command.construct(new SuggestionProviders<>()));
        });
    }

    public Chunky getChunky() {
        return chunky;
    }
}
