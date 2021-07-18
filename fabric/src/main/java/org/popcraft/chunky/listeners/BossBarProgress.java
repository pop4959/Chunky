package org.popcraft.chunky.listeners;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.platform.FabricWorld;

public class BossBarProgress {
    public static void tick(final Chunky chunky, final MinecraftServer server) {
        final int quietInterval = Math.max(1, chunky.getOptions().getQuietInterval() * 20);
        if (server.getTicks() % quietInterval != 0) {
            return;
        }
        for (ServerWorld world : server.getWorlds()) {
            final Identifier worldId = world.getRegistryKey().getValue();
            final Identifier barId = Identifier.tryParse("chunky:progress_" + worldId.toString().replace(':', '_'));
            if (barId == null) {
                continue;
            }
            final BossBarManager bossBarManager = server.getBossBarManager();
            final GenerationTask task = chunky.getGenerationTasks().get(new FabricWorld(world));
            final boolean barExists = bossBarManager.get(barId) != null;
            if (task == null && !barExists) {
                continue;
            }
            final CommandBossBar bossBar;
            if (barExists) {
                bossBar = bossBarManager.get(barId);
            } else {
                bossBar = bossBarManager.add(barId, Text.of(barId.toString()));
                if (DimensionType.OVERWORLD_ID.equals(worldId)) {
                    bossBar.setColor(BossBar.Color.GREEN);
                } else if (DimensionType.THE_NETHER_ID.equals(worldId)) {
                    bossBar.setColor(BossBar.Color.RED);
                } else if (DimensionType.THE_END_ID.equals(worldId)) {
                    bossBar.setColor(BossBar.Color.PURPLE);
                } else {
                    bossBar.setColor(BossBar.Color.BLUE);
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
                bossBar.clearPlayers();
                bossBarManager.remove(bossBar);
                continue;
            }
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.hasPermissionLevel(2)) {
                    bossBar.addPlayer(player);
                } else {
                    bossBar.removePlayer(player);
                }
            }
            final GenerationTask.Progress progress = task.getProgress();
            bossBar.setName(Text.of(String.format("%s | %s%% | %s:%s:%s",
                    worldId,
                    String.format("%.2f", progress.getPercentComplete()),
                    String.format("%01d", progress.getHours()),
                    String.format("%02d", progress.getMinutes()),
                    String.format("%02d", progress.getSeconds()))));
            bossBar.setPercent(task.getProgress().getPercentComplete() / 100f);
        }
    }
}
