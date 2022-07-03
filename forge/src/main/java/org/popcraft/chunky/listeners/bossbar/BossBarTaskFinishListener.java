package org.popcraft.chunky.listeners.bossbar;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.platform.ForgeWorld;
import org.popcraft.chunky.platform.World;

import java.util.function.Consumer;

public class BossBarTaskFinishListener implements Consumer<GenerationTaskFinishEvent> {
    @Override
    public void accept(final GenerationTaskFinishEvent event) {
        final GenerationTask task = event.getGenerationTask();
        final World world = task.getSelection().world();
        final ResourceLocation worldIdentifier = ResourceLocation.tryParse(world.getKey());
        final ResourceLocation barIdentifier = ResourceLocation.tryParse("chunky:progress_" + world.getKey().replace(':', '_'));
        if (worldIdentifier == null || barIdentifier == null || !(world instanceof ForgeWorld)) {
            return;
        }
        final MinecraftServer server = ((ForgeWorld) world).getWorld().getServer();
        final CustomBossEvents bossBarManager = server.getCustomBossEvents();
        final CustomBossEvent existingBossBar = bossBarManager.get(barIdentifier);
        if (existingBossBar != null) {
            existingBossBar.removeAllPlayers();
            bossBarManager.remove(existingBossBar);
        }
    }
}
