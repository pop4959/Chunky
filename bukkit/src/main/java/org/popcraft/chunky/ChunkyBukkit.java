package org.popcraft.chunky;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
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
import org.popcraft.chunky.platform.BukkitConfig;
import org.popcraft.chunky.platform.BukkitPlayer;
import org.popcraft.chunky.platform.BukkitSender;
import org.popcraft.chunky.platform.BukkitServer;
import org.popcraft.chunky.platform.Folia;
import org.popcraft.chunky.platform.Paper;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;
import org.popcraft.chunky.util.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.popcraft.chunky.util.Translator.translate;

public final class ChunkyBukkit extends JavaPlugin implements Listener {

    private static final String COMMAND_PERMISSION_KEY = "chunky.command.";
    private Chunky chunky;

    @Override
    public void onEnable() {
        this.chunky = new Chunky(new BukkitServer(this), new BukkitConfig(this));
        final Version currentVersion = new Version(Bukkit.getBukkitVersion(), true);
        if (currentVersion.isValid() && Version.MINECRAFT_1_13_2.isHigherThan(currentVersion)) {
            getLogger().severe(() -> translate(TranslationKey.ERROR_VERSION));
            getServer().getPluginManager().disablePlugin(this);
        } else if (Paper.isPaper() && !currentVersion.isHigherThanOrEqualTo(Version.MINECRAFT_1_21_1)) {
            getLogger().severe("This version of the Chunky plugin only support Paper versions 1.21.1 and above!");
            getLogger().severe("Please update your server or use an older version of the plugin instead.");
            getServer().getPluginManager().disablePlugin(this);
        }

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

        if (Paper.isPaper()) {
            Paper.registerCommand(this, chunky, BukkitSender::new, BukkitSender::new, COMMAND_PERMISSION_KEY);
        } else {
            disablePauseWhenEmptySeconds();
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        if (chunky != null) {
            chunky.disable();
        }
    }

    @EventHandler
    public void onWorldInit(final WorldInitEvent event) {
        chunky.getRegionCache().clear(event.getWorld().getName());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Sender bukkitSender = sender instanceof final Player player ? new BukkitPlayer(player) : new BukkitSender(sender);
        final Map<String, ChunkyCommand> commands = chunky.getCommands();
        final CommandArguments arguments = CommandArguments.of(Arrays.copyOfRange(args, Math.min(1, args.length), args.length));
        if (args.length > 0 && commands.containsKey(args[0].toLowerCase())) {
            if (sender.hasPermission(COMMAND_PERMISSION_KEY + args[0].toLowerCase())) {
                commands.get(args[0].toLowerCase()).execute(bukkitSender, arguments);
            } else {
                bukkitSender.sendMessage(TranslationKey.COMMAND_NO_PERMISSION);
            }
        } else {
            commands.get(CommandLiteral.HELP).execute(bukkitSender, arguments);
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

    private void disablePauseWhenEmptySeconds() {
        final Path serverPropertiesPath = Path.of(".").resolve("server.properties");
        final File serverPropertiesFile = serverPropertiesPath.toFile();
        final Properties serverProperties = new Properties();
        try (final FileInputStream serverPropertiesFileInputStream = new FileInputStream(serverPropertiesFile)) {
            serverProperties.load(serverPropertiesFileInputStream);
            final Optional<Integer> pauseWhenEmptySeconds = Input.tryInteger(serverProperties.getProperty("pause-when-empty-seconds"));
            if (pauseWhenEmptySeconds.isPresent() && pauseWhenEmptySeconds.get() > 0) {
                serverProperties.setProperty("pause-when-empty-seconds", "0");
                try (final FileOutputStream serverPropertiesFileOutputStream = new FileOutputStream(serverPropertiesFile)) {
                    serverProperties.store(serverPropertiesFileOutputStream, "Minecraft server properties");
                    getLogger().warning(() -> translate(TranslationKey.ERROR_PAUSE_WHEN_EMPTY_SECONDS));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public Chunky getChunky() {
        return chunky;
    }
}
