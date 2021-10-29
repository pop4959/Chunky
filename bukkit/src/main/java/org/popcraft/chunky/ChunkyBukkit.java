package org.popcraft.chunky;

import io.papermc.lib.PaperLib;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.integration.WorldBorderIntegration;
import org.popcraft.chunky.platform.BukkitConfig;
import org.popcraft.chunky.platform.BukkitPlayer;
import org.popcraft.chunky.platform.BukkitSender;
import org.popcraft.chunky.platform.BukkitServer;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.TranslationKey;
import org.popcraft.chunky.util.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.popcraft.chunky.util.Translator.translate;

public final class ChunkyBukkit extends JavaPlugin {
    private static final String COMMAND_PERMISSION_KEY = "chunky.command.";
    private Chunky chunky;

    @Override
    public void onEnable() {
        this.chunky = new Chunky(new BukkitServer(this), new BukkitConfig(this));
        final Version currentVersion = new Version(Bukkit.getBukkitVersion(), true);
        if (Version.MINECRAFT_1_13_2.isEqualTo(currentVersion) && !PaperLib.isPaper()) {
            getLogger().severe(() -> translate(TranslationKey.ERROR_VERSION_SPIGOT));
            getServer().getPluginManager().disablePlugin(this);
        } else if (currentVersion.isValid() && Version.MINECRAFT_1_13_2.isHigherThan(currentVersion)) {
            getLogger().severe(() -> translate(TranslationKey.ERROR_VERSION));
            getServer().getPluginManager().disablePlugin(this);
        }
        if (!isEnabled()) {
            return;
        }
        if (chunky.getConfig().getContinueOnRestart()) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> chunky.getCommands().get(CommandLiteral.CONTINUE).execute(chunky.getServer().getConsole(), new String[]{}));
        }
        if (getServer().getPluginManager().getPlugin("WorldBorder") != null) {
            chunky.getServer().getIntegrations().put("border", new WorldBorderIntegration());
        }
        final Metrics metrics = new Metrics(this, 8211);
        metrics.addCustomChart(new SimplePie("language", () -> chunky.getConfig().getLanguage()));
    }

    @Override
    public void onDisable() {
        chunky.disable();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Sender bukkitSender = sender instanceof Player ? new BukkitPlayer((Player) sender) : new BukkitSender(sender);
        Map<String, ChunkyCommand> commands = chunky.getCommands();
        if (args.length > 0 && commands.containsKey(args[0].toLowerCase())) {
            if (sender.hasPermission(COMMAND_PERMISSION_KEY + args[0].toLowerCase())) {
                commands.get(args[0].toLowerCase()).execute(bukkitSender, args);
            } else {
                bukkitSender.sendMessage(TranslationKey.COMMAND_NO_PERMISSION);
            }
        } else {
            commands.get(CommandLiteral.HELP).execute(bukkitSender, new String[]{});
        }
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 1) {
            return Collections.emptyList();
        }
        final List<String> suggestions = new ArrayList<>();
        Map<String, ChunkyCommand> commands = chunky.getCommands();
        if (args.length == 1) {
            commands.keySet().stream().filter(name -> sender.hasPermission(COMMAND_PERMISSION_KEY + name)).forEach(suggestions::add);
        } else if (commands.containsKey(args[0].toLowerCase()) && sender.hasPermission(COMMAND_PERMISSION_KEY + args[0].toLowerCase())) {
            suggestions.addAll(commands.get(args[0].toLowerCase()).tabSuggestions(args));
        }
        return suggestions.stream()
                .filter(s -> s.toLowerCase().contains(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    public Chunky getChunky() {
        return chunky;
    }
}
