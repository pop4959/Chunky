package org.popcraft.chunky;

import org.popcraft.chunky.platform.Config;
import org.popcraft.chunky.platform.Server;

import static org.popcraft.chunky.Chunky.translate;

public class GenerationTaskSleepManager implements Runnable {

    CommonTpsService tpsService;
    Chunky chunky;
    Server server;
    Config config;
    String restartKey;
    boolean sleeping;

    public GenerationTaskSleepManager(Chunky chunky) {
        this.tpsService = new CommonTpsService();
        this.chunky = chunky;
    }

    public void start() {
        this.server = chunky.getPlatform().getServer();
        this.config = chunky.getConfig();
        this.server.getScheduler().runTaskSyncTimer(this, 1);
    }


    @Override
    public void run() {
        boolean playersEnabled = config.getWatchdogEnabled("players");
        boolean tpsEnabled = config.getWatchdogEnabled("tps");

        if (tpsEnabled) {
            tpsService.saveTickTime();
        }

        // Do nothing else if there are no tasks
        if (chunky.getGenerationTasks().isEmpty()) {
            return;
        }

        boolean playersAllowsRun = config.getWatchdogStartOn("players") >= server.getPlayerCount();
        boolean tpsAllowsRun = tpsService.getTPS() >= config.getWatchdogStartOn("tps");

        if (!playersAllowsRun && playersEnabled) {
            if (!sleeping) {
                sleeping = true;
                server.getConsoleSender().sendMessage("stop_player_online", translate("prefix"));
                this.restartKey = "start_no_players";
                sleepTasks();
            }
        } else if (!tpsAllowsRun && tpsEnabled) {
            if (!sleeping) {
                sleeping = true;
                server.getConsoleSender().sendMessage("stop_tps_low", translate("prefix"));
                this.restartKey = "start_tps_high";
                sleepTasks();
            }
        } else {
            if (sleeping) {
                sleeping = false;
                server.getConsoleSender().sendMessage(restartKey, translate("prefix"));
                wakeTasks();
            }
        }
    }

    private void sleepTasks() {
        for (GenerationTask task : chunky.getGenerationTasks().values()) {
            try {
                task.sleep();
            } catch (InterruptedException ignored) {
                return;
            }
        }
    }

    private void wakeTasks() {
        chunky.getGenerationTasks().values().forEach(GenerationTask::wake);
    }
}