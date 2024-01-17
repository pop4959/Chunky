package org.popcraft.chunky.listeners.bossbar;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.platform.FabricWorld;
import org.popcraft.chunky.platform.World;

import java.util.Map;
import java.util.function.Consumer;

public class BossBarTaskUpdateListener implements Consumer<GenerationTaskUpdateEvent> {
    private final Map<Identifier, ServerBossBar> bossBars;

    public BossBarTaskUpdateListener(final Map<Identifier, ServerBossBar> bossBars) {
        this.bossBars = bossBars;
    }

    @Override
    public void accept(final GenerationTaskUpdateEvent event) {
        final GenerationTask task = event.generationTask();
        final Chunky chunky = task.getChunky();
        final World world = task.getSelection().world();
        final Identifier worldIdentifier = Identifier.tryParse(world.getKey());
        if (worldIdentifier == null || !(world instanceof final FabricWorld fabricWorld)) {
            return;
        }
        final ServerBossBar bossBar = bossBars.computeIfAbsent(worldIdentifier, x -> createNewBossBar(worldIdentifier));
        final boolean silent = chunky.getConfig().isSilent();
        if (silent == bossBar.isVisible()) {
            bossBar.setVisible(!silent);
        }
        final MinecraftServer server = fabricWorld.getServerWorld().getServer();
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
            bossBars.remove(worldIdentifier);
        }
    }

    private ServerBossBar createNewBossBar(final Identifier worldIdentifier) {
        final ServerBossBar bossBar = new ServerBossBar(
                Text.of(worldIdentifier.toString()),
                bossBarColor(worldIdentifier),
                BossBar.Style.PROGRESS
        );
        bossBar.setDarkenSky(false);
        bossBar.setDragonMusic(false);
        bossBar.setThickenFog(false);
        return bossBar;
    }

    private static BossBar.Color bossBarColor(Identifier worldIdentifier) {
        final BossBar.Color bossBarColor;
        if (net.minecraft.world.World.OVERWORLD.getValue().equals(worldIdentifier)) {
            bossBarColor = BossBar.Color.GREEN;
        } else if (net.minecraft.world.World.NETHER.getValue().equals(worldIdentifier)) {
            bossBarColor = BossBar.Color.RED;
        } else if (net.minecraft.world.World.END.getValue().equals(worldIdentifier)) {
            bossBarColor = BossBar.Color.PURPLE;
        } else {
            bossBarColor = BossBar.Color.BLUE;
        }
        return bossBarColor;
    }
}
