package org.popcraft.chunky.listeners.bossbar;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.platform.FabricWorld;
import org.popcraft.chunky.platform.World;

import java.util.function.Consumer;

public class BossBarTaskUpdateListener implements Consumer<GenerationTaskUpdateEvent> {
    @Override
    public void accept(final GenerationTaskUpdateEvent event) {
        final GenerationTask task = event.getGenerationTask();
        final Chunky chunky = task.getChunky();
        final World world = task.getSelection().world();
        final Identifier worldIdentifier = Identifier.tryParse(world.getKey());
        final Identifier barIdentifier = Identifier.tryParse("chunky:progress_" + world.getKey().replace(':', '_'));
        if (worldIdentifier == null || barIdentifier == null || !(world instanceof FabricWorld)) {
            return;
        }
        final MinecraftServer server = ((FabricWorld) world).getServerWorld().getServer();
        final BossBarManager bossBarManager = server.getBossBarManager();
        final CommandBossBar existingBossBar = bossBarManager.get(barIdentifier);
        final CommandBossBar bossBar = existingBossBar == null ? createNewBossBar(bossBarManager, barIdentifier, worldIdentifier) : existingBossBar;
        final boolean silent = chunky.getConfig().isSilent();
        if (silent == bossBar.isVisible()) {
            bossBar.setVisible(!silent);
        }
        for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.hasPermissionLevel(2)) {
                bossBar.addPlayer(player);
            } else {
                bossBar.removePlayer(player);
            }
        }
        final GenerationTask.Progress progress = task.getProgress();
        bossBar.setName(Text.of(String.format("%s | %s%% | %s:%s:%s",
                worldIdentifier,
                String.format("%.2f", progress.getPercentComplete()),
                String.format("%01d", progress.getHours()),
                String.format("%02d", progress.getMinutes()),
                String.format("%02d", progress.getSeconds()))));
        bossBar.setPercent(progress.getPercentComplete() / 100f);
        if (progress.isComplete()) {
            bossBar.clearPlayers();
            bossBarManager.remove(bossBar);
        }
    }

    private CommandBossBar createNewBossBar(final BossBarManager bossBarManager, final Identifier barIdentifier, final Identifier worldIdentifier) {
        final CommandBossBar bossBar = bossBarManager.add(barIdentifier, Text.of(barIdentifier.toString()));
        if (net.minecraft.world.World.OVERWORLD.getValue().equals(worldIdentifier)) {
            bossBar.setColor(BossBar.Color.GREEN);
        } else if (net.minecraft.world.World.NETHER.getValue().equals(worldIdentifier)) {
            bossBar.setColor(BossBar.Color.RED);
        } else if (net.minecraft.world.World.END.getValue().equals(worldIdentifier)) {
            bossBar.setColor(BossBar.Color.PURPLE);
        } else {
            bossBar.setColor(BossBar.Color.BLUE);
        }
        return bossBar;
    }
}
