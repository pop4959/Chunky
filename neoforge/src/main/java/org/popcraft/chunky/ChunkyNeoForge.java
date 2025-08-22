package org.popcraft.chunky;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.brigadier.NeoForgeChunkyCommand;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskFinishListener;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskUpdateListener;
import org.popcraft.chunky.platform.NeoForgeServer;
import org.popcraft.chunky.platform.impl.GsonConfig;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        NeoForgeChunkyCommand command = new NeoForgeChunkyCommand(chunky);
        event.getDispatcher().register(command.construct(new SuggestionProviders<>()));
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
