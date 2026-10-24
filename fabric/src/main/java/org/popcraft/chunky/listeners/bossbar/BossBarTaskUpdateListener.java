package org.popcraft.chunky.listeners.bossbar;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskUpdateEvent;
import org.popcraft.chunky.platform.FabricWorld;
import org.popcraft.chunky.platform.World;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BossBarTaskUpdateListener implements Consumer<GenerationTaskUpdateEvent> {
    private static final boolean HAS_PERMISSIONS;

    static {
        boolean hasPermissions;
        try {
            Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
            hasPermissions = true;
        } catch (ClassNotFoundException e) {
            hasPermissions = false;
        }
        HAS_PERMISSIONS = hasPermissions;
    }

    private final Map<Identifier, ServerBossEvent> bossBars;

    public BossBarTaskUpdateListener(final Map<Identifier, ServerBossEvent> bossBars) {
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
        final ServerBossEvent bossBar = bossBars.computeIfAbsent(worldIdentifier, x -> createNewBossBar(worldIdentifier));
        final boolean silent = chunky.getConfig().isSilent();
        if (silent == bossBar.isVisible()) {
            bossBar.setVisible(!silent);
        }
        final MinecraftServer server = fabricWorld.getWorld().getServer();
        for (final ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (hasBossBarPermission(server, player)) {
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
        bossBar.setProgress(progress.getPercentComplete() / 100f);
        if (progress.isComplete()) {
            bossBar.removeAllPlayers();
            bossBars.remove(worldIdentifier);
        }
    }

    private static boolean hasBossBarPermission(final MinecraftServer server, final ServerPlayer player) {
        if (server != null && server.isSingleplayer()) {
            return true;
        }
        if (HAS_PERMISSIONS) {
            return (Permissions.check(player, "chunky.command", 2)
                    || Permissions.check(player, "chunky.progressbar", 2));
        }
        return player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
    }

    private ServerBossEvent createNewBossBar(final Identifier worldIdentifier) {
        final ServerBossEvent bossBar = new ServerBossEvent(
                UUID.randomUUID(),
                Component.nullToEmpty(worldIdentifier.toString()),
                bossBarColor(worldIdentifier),
                BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.setDarkenScreen(false);
        bossBar.setPlayBossMusic(false);
        bossBar.setCreateWorldFog(false);
        return bossBar;
    }

    private static BossEvent.BossBarColor bossBarColor(Identifier worldIdentifier) {
        final BossEvent.BossBarColor bossBarColor;
        if (Level.OVERWORLD.identifier().equals(worldIdentifier)) {
            bossBarColor = BossEvent.BossBarColor.GREEN;
        } else if (Level.NETHER.identifier().equals(worldIdentifier)) {
            bossBarColor = BossEvent.BossBarColor.RED;
        } else if (Level.END.identifier().equals(worldIdentifier)) {
            bossBarColor = BossEvent.BossBarColor.PURPLE;
        } else {
            bossBarColor = BossEvent.BossBarColor.BLUE;
        }
        return bossBarColor;
    }
}
