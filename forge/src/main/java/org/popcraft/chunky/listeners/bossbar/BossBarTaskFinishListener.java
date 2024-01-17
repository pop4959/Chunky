package org.popcraft.chunky.listeners.bossbar;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.platform.World;

import java.util.Map;
import java.util.function.Consumer;

public class BossBarTaskFinishListener implements Consumer<GenerationTaskFinishEvent> {
    private final Map<ResourceLocation, ServerBossEvent> bossBars;

    public BossBarTaskFinishListener(final Map<ResourceLocation, ServerBossEvent> bossBars) {
        this.bossBars = bossBars;
    }

    @Override
    public void accept(final GenerationTaskFinishEvent event) {
        final GenerationTask task = event.generationTask();
        final World world = task.getSelection().world();
        final ResourceLocation worldIdentifier = ResourceLocation.tryParse(world.getKey());
        if (worldIdentifier == null) {
            return;
        }
        final ServerBossEvent bossBar = bossBars.get(worldIdentifier);
        if (bossBar != null) {
            bossBar.removeAllPlayers();
            bossBars.remove(worldIdentifier);
        }
    }
}
