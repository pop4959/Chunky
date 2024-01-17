package org.popcraft.chunky.listeners.bossbar;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.util.Identifier;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.event.task.GenerationTaskFinishEvent;
import org.popcraft.chunky.platform.World;

import java.util.Map;
import java.util.function.Consumer;

public class BossBarTaskFinishListener implements Consumer<GenerationTaskFinishEvent> {
    private final Map<Identifier, ServerBossBar> bossBars;

    public BossBarTaskFinishListener(final Map<Identifier, ServerBossBar> bossBars) {
        this.bossBars = bossBars;
    }

    @Override
    public void accept(final GenerationTaskFinishEvent event) {
        final GenerationTask task = event.generationTask();
        final World world = task.getSelection().world();
        final Identifier worldIdentifier = Identifier.tryParse(world.getKey());
        if (worldIdentifier == null) {
            return;
        }
        final ServerBossBar bossBar = bossBars.get(worldIdentifier);
        if (bossBar != null) {
            bossBar.clearPlayers();
            bossBars.remove(worldIdentifier);
        }
    }
}
