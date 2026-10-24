package org.popcraft.chunky;

import io.papermc.paper.threadedregions.RegionizedServerInitEvent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.integration.WorldBorderIntegration;
import org.popcraft.chunky.platform.PaperConfig;
import org.popcraft.chunky.platform.PaperPlayer;
import org.popcraft.chunky.platform.PaperSender;
import org.popcraft.chunky.platform.PaperServer;
import org.popcraft.chunky.platform.PaperWorld;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.TaskMetrics;
import org.popcraft.chunky.util.TranslationKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ChunkyPaper extends JavaPlugin implements Listener {
    private static final String COMMAND_PERMISSION_KEY = "chunky.command.";
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
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        if (chunky != null) {
            chunky.disable();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Sender paperSender = sender instanceof final Player player ? new PaperPlayer(player) : new PaperSender(sender);
        final Map<String, ChunkyCommand> commands = chunky.getCommands();
        final CommandArguments arguments = CommandArguments.of(Arrays.copyOfRange(args, Math.min(1, args.length), args.length));
        if (args.length > 0 && commands.containsKey(args[0].toLowerCase())) {
            if (sender.hasPermission(COMMAND_PERMISSION_KEY + args[0].toLowerCase())) {
                commands.get(args[0].toLowerCase()).execute(paperSender, arguments);
            } else {
                paperSender.sendMessage(TranslationKey.COMMAND_NO_PERMISSION);
            }
        } else {
            commands.get(CommandLiteral.HELP).execute(paperSender, arguments);
        }
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length < 1) {
            return List.of();
        }
        final List<String> suggestions = new ArrayList<>();
        final Map<String, ChunkyCommand> commands = chunky.getCommands();
        if (args.length == 1) {
            commands.keySet().stream().filter(name -> sender.hasPermission(COMMAND_PERMISSION_KEY + name)).forEach(suggestions::add);
        } else if (commands.containsKey(args[0].toLowerCase()) && sender.hasPermission(COMMAND_PERMISSION_KEY + args[0].toLowerCase())) {
            final CommandArguments arguments = CommandArguments.of(Arrays.copyOfRange(args, 1, args.length));
            suggestions.addAll(commands.get(args[0].toLowerCase()).suggestions(arguments));
        }
        return suggestions.stream()
                .filter(s -> s.toLowerCase().contains(args[args.length - 1].toLowerCase()))
                .toList();
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
