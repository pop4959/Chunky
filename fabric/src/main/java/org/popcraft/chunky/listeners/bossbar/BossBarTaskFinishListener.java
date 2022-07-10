package org.popcraft.chunky.listeners.bossbar;

import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.platform.FabricWorld;
import org.popcraft.chunky.platform.World;

import java.util.function.Consumer;

public class BossBarTaskFinishListener implements Consumer<GenerationTaskFinishEvent> {
    @Override
    public void accept(final GenerationTaskFinishEvent event) {
        final GenerationTask task = event.generationTask();
        final World world = task.getSelection().world();
        final Identifier worldIdentifier = Identifier.tryParse(world.getKey());
        final Identifier barIdentifier = Identifier.tryParse("chunky:progress_" + world.getKey().replace(':', '_'));
        if (worldIdentifier == null || barIdentifier == null || !(world instanceof FabricWorld)) {
            return;
        }
        final MinecraftServer server = ((FabricWorld) world).getServerWorld().getServer();
        final BossBarManager bossBarManager = server.getBossBarManager();
        final CommandBossBar existingBossBar = bossBarManager.get(barIdentifier);
        if (existingBossBar != null) {
            existingBossBar.clearPlayers();
            bossBarManager.remove(existingBossBar);
        }
    }
}
