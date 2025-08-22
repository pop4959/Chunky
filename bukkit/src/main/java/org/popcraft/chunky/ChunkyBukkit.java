package org.popcraft.chunky;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.command.CommandLiteral;
import org.popcraft.chunky.platform.BukkitPlayer;
import org.popcraft.chunky.platform.BukkitSender;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.popcraft.chunky.util.Translator.translate;

public final class ChunkyBukkit extends AbstractChunkyBukkit {

    private static final String COMMAND_PERMISSION_KEY = "chunky.command.";

    @Override
    protected void postEnable() {
        disablePauseWhenEmptySeconds();
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
}
