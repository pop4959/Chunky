package org.popcraft.chunky.listeners.bossbar;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.platform.ForgeWorld;
import org.popcraft.chunky.platform.World;

import java.util.Map;
import java.util.function.Consumer;

public class BossBarTaskUpdateListener implements Consumer<GenerationTaskUpdateEvent> {
    private final Map<ResourceLocation, ServerBossEvent> bossBars;

    public BossBarTaskUpdateListener(final Map<ResourceLocation, ServerBossEvent> bossBars) {
        this.bossBars = bossBars;
    }

    @Override
    public void accept(final GenerationTaskUpdateEvent event) {
        final GenerationTask task = event.generationTask();
        final Chunky chunky = task.getChunky();
        final World world = task.getSelection().world();
        final ResourceLocation worldIdentifier = ResourceLocation.tryParse(world.getKey());
        if (worldIdentifier == null || !(world instanceof final ForgeWorld forgeWorld)) {
            return;
        }
        final ServerBossEvent bossBar = bossBars.computeIfAbsent(worldIdentifier, x -> createNewBossBar(worldIdentifier));
        final boolean silent = chunky.getConfig().isSilent();
        if (silent == bossBar.isVisible()) {
            bossBar.setVisible(!silent);
        }
        final MinecraftServer server = forgeWorld.getWorld().getServer();
        for (final ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.hasPermissions(2)) {
                bossBar.addPlayer(player);
            } else {
                bossBar.removePlayer(player);
            }
        }
        final GenerationTask.Progress progress = task.getProgress();
        bossBar.setName(Component.nullToEmpty(String.format("%s | %s%% | %s:%s:%s",
                worldIdentifier,
                String.format("%.2f", progress.getPercentComplete()),
                String.format("%01d", progress.getHours()),
                String.format("%02d", progress.getMinutes()),
                String.format("%02d", progress.getSeconds()))));
        bossBar.setProgress(task.getProgress().getPercentComplete() / 100f);
        if (progress.isComplete()) {
            bossBar.removeAllPlayers();
            bossBars.remove(worldIdentifier);
        }
    }

    private ServerBossEvent createNewBossBar(final ResourceLocation worldIdentifier) {
        final ServerBossEvent bossBar = new ServerBossEvent(
                Component.nullToEmpty(worldIdentifier.toString()),
                bossBarColor(worldIdentifier),
                BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.setDarkenScreen(false);
        bossBar.setPlayBossMusic(false);
        bossBar.setCreateWorldFog(false);
        return bossBar;
    }

    private static BossEvent.BossBarColor bossBarColor(ResourceLocation worldIdentifier) {
        final BossEvent.BossBarColor bossBarColor;
        if (Level.OVERWORLD.location().equals(worldIdentifier)) {
            bossBarColor = BossEvent.BossBarColor.GREEN;
        } else if (Level.NETHER.location().equals(worldIdentifier)) {
            bossBarColor = BossEvent.BossBarColor.RED;
        } else if (Level.END.location().equals(worldIdentifier)) {
            bossBarColor = BossEvent.BossBarColor.PURPLE;
        } else {
            bossBarColor = BossEvent.BossBarColor.BLUE;
        }
        return bossBarColor;
    }
}
