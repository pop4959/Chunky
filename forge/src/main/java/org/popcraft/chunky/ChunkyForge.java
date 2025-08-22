package org.popcraft.chunky;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.commands.brigadier.ForgeChunkyCommand;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskFinishListener;
import org.popcraft.chunky.listeners.bossbar.BossBarTaskUpdateListener;
import org.popcraft.chunky.platform.ForgeServer;
import org.popcraft.chunky.platform.impl.GsonConfig;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod(ChunkyForge.MOD_ID)
public class ChunkyForge {
    public static final String MOD_ID = "chunky";
    private Chunky chunky;
    private final Map<ResourceLocation, ServerBossEvent> bossBars = new ConcurrentHashMap<>();

    public ChunkyForge() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        final MinecraftServer server = event.getServer();
        final Path configPath = FMLPaths.CONFIGDIR.get().resolve("chunky/config.json");
        this.chunky = new Chunky(new ForgeServer(this, server), new GsonConfig(configPath));
        if (chunky.getConfig().getContinueOnRestart()) {
            chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), CommandArguments.empty());
        }
        chunky.getEventBus().subscribe(GenerationTaskUpdateEvent.class, new BossBarTaskUpdateListener(bossBars));
        chunky.getEventBus().subscribe(GenerationTaskFinishEvent.class, new BossBarTaskFinishListener(bossBars));
    }

    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        final ForgeChunkyCommand command = new ForgeChunkyCommand(this::getChunky);
        event.getDispatcher().register(command.construct(new SuggestionProviders<>()));
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
