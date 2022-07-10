package org.popcraft.chunky.listeners.bossbar;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.platform.ForgeWorld;
import org.popcraft.chunky.platform.World;

import java.util.function.Consumer;

public class BossBarTaskUpdateListener implements Consumer<GenerationTaskUpdateEvent> {
    @Override
    public void accept(final GenerationTaskUpdateEvent event) {
        final GenerationTask task = event.generationTask();
        final Chunky chunky = task.getChunky();
        final World world = task.getSelection().world();
        final ResourceLocation worldIdentifier = ResourceLocation.tryParse(world.getKey());
        final ResourceLocation barIdentifier = ResourceLocation.tryParse("chunky:progress_" + world.getKey().replace(':', '_'));
        if (worldIdentifier == null || barIdentifier == null || !(world instanceof ForgeWorld)) {
            return;
        }
        final MinecraftServer server = ((ForgeWorld) world).getWorld().getServer();
        final CustomBossEvents bossBarManager = server.getCustomBossEvents();
        final CustomBossEvent existingBossBar = bossBarManager.get(barIdentifier);
        final CustomBossEvent bossBar = existingBossBar == null ? createNewBossBar(bossBarManager, barIdentifier, worldIdentifier) : existingBossBar;
        final boolean silent = chunky.getConfig().isSilent();
        if (silent == bossBar.isVisible()) {
            bossBar.setVisible(!silent);
        }
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
            bossBarManager.remove(bossBar);
        }
    }

    private CustomBossEvent createNewBossBar(final CustomBossEvents bossBarManager, final ResourceLocation barIdentifier, final ResourceLocation worldIdentifier) {
        final CustomBossEvent bossBar = bossBarManager.create(barIdentifier, Component.nullToEmpty(barIdentifier.toString()));
        if (Level.OVERWORLD.location().equals(worldIdentifier)) {
            bossBar.setColor(BossEvent.BossBarColor.GREEN);
        } else if (Level.NETHER.location().equals(worldIdentifier)) {
            bossBar.setColor(BossEvent.BossBarColor.RED);
        } else if (Level.END.location().equals(worldIdentifier)) {
            bossBar.setColor(BossEvent.BossBarColor.PURPLE);
        } else {
            bossBar.setColor(BossEvent.BossBarColor.BLUE);
        }
        return bossBar;
    }
}
