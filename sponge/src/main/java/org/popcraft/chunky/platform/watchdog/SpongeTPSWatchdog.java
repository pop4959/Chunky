package org.popcraft.chunky.platform.watchdog;

import org.popcraft.chunky.ChunkySponge;
import org.popcraft.chunky.watchdog.CommonTpsService;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class SpongeTPSWatchdog extends TPSWatchdog {

    private CommonTpsService service;
    private Task task;

    public SpongeTPSWatchdog(ChunkySponge chunky, CommonTpsService tpsService) {
        super(chunky.getChunky());
        this.service = tpsService;
        this.task = Task.builder()
                .execute(service::saveTickTime)
                .intervalTicks(1)
                .name("Chunky - Save TPS")
                .submit(chunky);
    }

    @Override
    public boolean allowsGenerationRun() {
        return super.getConfiguredTPS() >= service.getTPS();
    }

    @Override
    public void stop() {
        if(task != null) {
            task.cancel();
        }
    }
}
