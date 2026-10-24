package org.popcraft.chunky;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.RegionizedServerInitEvent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
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
import org.popcraft.chunky.command.suggestion.SuggestionProviders;
import org.popcraft.chunky.integration.WorldBorderIntegration;
import org.popcraft.chunky.platform.*;

import java.util.List;

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
        getServer().getPluginManager().registerEvents(this, this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final PaperChunkyCommand command = new PaperChunkyCommand(chunky);
            event.registrar().register(command.construct(new SuggestionProviders<>()).build(), "Generates chunks", List.of("cy"));
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
