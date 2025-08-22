package org.popcraft.chunky;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
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
import org.popcraft.chunky.integration.WorldBorderIntegration;
import org.popcraft.chunky.platform.BukkitConfig;
import org.popcraft.chunky.platform.BukkitServer;
import org.popcraft.chunky.platform.Folia;
import org.popcraft.chunky.util.TranslationKey;
import org.popcraft.chunky.util.Version;

import static org.popcraft.chunky.util.Translator.translate;

public abstract class AbstractChunkyBukkit extends JavaPlugin implements Listener {

    protected static final String COMMAND_PERMISSION_KEY = "chunky.command.";
    protected Chunky chunky;

    protected abstract void postEnable();

    protected void validateServerVersion(Version version) {
        if (version.isValid() && Version.MINECRAFT_1_13_2.isHigherThan(version)) {
            getLogger().severe(() -> translate(TranslationKey.ERROR_VERSION));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public final void onEnable() {
        this.chunky = new Chunky(new BukkitServer(this), new BukkitConfig(this));
        final Version currentVersion = new Version(Bukkit.getBukkitVersion(), true);
        validateServerVersion(currentVersion);

        if (!isEnabled()) {
            return;
        }

        getServer().getServicesManager().register(Chunky.class, chunky, this, ServicePriority.Normal);
        getServer().getServicesManager().register(ChunkyAPI.class, chunky.getApi(), this, ServicePriority.Normal);
        if (chunky.getConfig().getContinueOnRestart()) {
            final Runnable continueTask = () -> chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), CommandArguments.empty());
            if (Folia.isFolia()) {
                Folia.onServerInit(this, continueTask);
            } else {
                getServer().getScheduler().scheduleSyncDelayedTask(this, continueTask);
            }
        }
        if (getServer().getPluginManager().getPlugin("WorldBorder") != null) {
            chunky.getServer().getIntegrations().put("border", new WorldBorderIntegration());
        }
        final Metrics metrics = new Metrics(this, 8211);
        metrics.addCustomChart(new SimplePie("language", () -> chunky.getConfig().getLanguage()));
        getServer().getPluginManager().registerEvents(this, this);

        postEnable();
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
}
