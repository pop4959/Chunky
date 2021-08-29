package org.popcraft.chunky.listeners;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.dimension.DimensionType;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.ForgeWorld;
import org.popcraft.chunky.platform.World;

public class BossBarProgress {
    private BossBarProgress() {
    }

    public static void tick(final Chunky chunky, final MinecraftServer server) {
        final int quietInterval = Math.max(1, chunky.getOptions().getQuietInterval() * 20);
        if (server.getTickCount() % quietInterval != 0) {
            return;
        }
        for (ServerLevel world : server.getAllLevels()) {
            final ResourceLocation worldId = world.dimension().location();
            final ResourceLocation barId = ResourceLocation.tryParse("chunky:progress_" + worldId.toString().replace(':', '_'));
            if (barId == null) {
                continue;
            }
            final CustomBossEvents bossBarManager = server.getCustomBossEvents();
            final World forgeWorld = new ForgeWorld(world);
            final GenerationTask task = chunky.getGenerationTasks().get(forgeWorld.getName());
            final boolean barExists = bossBarManager.get(barId) != null;
            if (task == null && !barExists) {
                continue;
            }
            final CustomBossEvent bossBar;
            if (barExists) {
                bossBar = bossBarManager.get(barId);
            } else {
                bossBar = bossBarManager.create(barId, Component.nullToEmpty(barId.toString()));
                if (DimensionType.OVERWORLD_EFFECTS.equals(worldId)) {
                    bossBar.setColor(BossEvent.BossBarColor.GREEN);
                } else if (DimensionType.NETHER_EFFECTS.equals(worldId)) {
                    bossBar.setColor(BossEvent.BossBarColor.RED);
                } else if (DimensionType.END_EFFECTS.equals(worldId)) {
                    bossBar.setColor(BossEvent.BossBarColor.PURPLE);
                } else {
                    bossBar.setColor(BossEvent.BossBarColor.BLUE);
                }
            }
            if (bossBar == null) {
                continue;
            }
            final boolean silent = chunky.getOptions().isSilent();
            if (silent && bossBar.isVisible() || !silent && !bossBar.isVisible()) {
                bossBar.setVisible(!silent);
            }
            if (task == null) {
                bossBar.removeAllPlayers();
                bossBarManager.remove(bossBar);
                continue;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.hasPermissions(2)) {
                    bossBar.addPlayer(player);
                } else {
                    bossBar.removePlayer(player);
                }
            }
            final GenerationTask.Progress progress = task.getProgress();
            bossBar.setName(Component.nullToEmpty(String.format("%s | %s%% | %s:%s:%s",
                    worldId,
                    String.format("%.2f", progress.getPercentComplete()),
                    String.format("%01d", progress.getHours()),
                    String.format("%02d", progress.getMinutes()),
                    String.format("%02d", progress.getSeconds()))));
            bossBar.setProgress(task.getProgress().getPercentComplete() / 100f);
        }
    }
}
