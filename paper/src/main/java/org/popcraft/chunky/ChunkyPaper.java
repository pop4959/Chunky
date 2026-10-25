package org.popcraft.chunky;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.RegionizedServerInitEvent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.command.brigadier.PaperCommand;
import org.popcraft.chunky.integration.WorldBorderIntegration;
import org.popcraft.chunky.platform.PaperConfig;
import org.popcraft.chunky.platform.PaperServer;
import org.popcraft.chunky.platform.PaperWorld;
import org.popcraft.chunky.util.TaskMetrics;

import java.util.List;
import java.util.Map;

public final class ChunkyPaper extends JavaPlugin implements Listener {
    private Chunky chunky;

    @Override
    public void onEnable() {
        this.chunky = new Chunky(new PaperServer(this), new PaperConfig(this));
        getServer().getServicesManager().register(Chunky.class, chunky, this, ServicePriority.Normal);
        getServer().getServicesManager().register(ChunkyAPI.class, chunky.getApi(), this, ServicePriority.Normal);
        if (getServer().getPluginManager().getPlugin("WorldBorder") != null) {
            chunky.getServer().getIntegrations().put("border", new WorldBorderIntegration());
        }
        final Metrics metrics = new Metrics(this, 8211);
        metrics.addCustomChart(new SimplePie("language", () -> chunky.getConfig().getLanguage()));
        metrics.addCustomChart(new DrilldownPie("platform", () -> Map.of("Paper", Map.of(getPluginMeta().getVersion(), 1))));
        metrics.addCustomChart(new SingleLineChart("active_tasks", () -> TaskMetrics.activeTasks(chunky)));
        metrics.addCustomChart(new SingleLineChart("chunks_per_second", () -> TaskMetrics.chunksPerSecond(chunky)));
        metrics.addCustomChart(new AdvancedPie("shape", () -> TaskMetrics.shapes(chunky)));
        metrics.addCustomChart(new AdvancedPie("pattern", () -> TaskMetrics.patterns(chunky)));
        metrics.addCustomChart(new AdvancedPie("size", () -> TaskMetrics.sizes(chunky)));
        metrics.addCustomChart(new AdvancedPie("speed", () -> TaskMetrics.speeds(chunky)));
        metrics.addCustomChart(new AdvancedPie("dimension", () -> TaskMetrics.count(chunky, selection -> ((PaperWorld) selection.world()).getDimension())));
        getServer().getPluginManager().registerEvents(this, this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final PaperCommand command = new PaperCommand(chunky);
            event.registrar().register(command.build().build(), "Generates chunks", List.of("cy"));
        });
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        if (chunky != null) {
            chunky.disable();
        }
    }

    public Chunky getChunky() {
        return chunky;
    }

    @EventHandler
    public void onWorldInit(final WorldInitEvent event) {
        chunky.getRegionCache().clear(event.getWorld().getName());
    }

    @EventHandler
    public void onRegionizedServerInit(final RegionizedServerInitEvent event) {
        if (chunky.getConfig().getContinueOnRestart()) {
            chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), CommandArguments.empty());
        }
    }
}
