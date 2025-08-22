package org.popcraft.chunky.platform;

import org.bukkit.configuration.file.FileConfigurationOptions;
import org.popcraft.chunky.AbstractChunkyBukkit;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Translator;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class BukkitConfig implements Config {
    private static final List<String> HEADER = Arrays.asList("Chunky Configuration", "https://github.com/pop4959/Chunky/wiki/Configuration");
    private final AbstractChunkyBukkit plugin;

    public BukkitConfig(final AbstractChunkyBukkit plugin) {
        this.plugin = plugin;
        final FileConfigurationOptions options = plugin.getConfig().options();
        options.copyDefaults(true);
        try {
            FileConfigurationOptions.class.getMethod("header", String.class).invoke(options, String.join("\n", HEADER));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            options.setHeader(HEADER);
        }
        plugin.saveConfig();
        Translator.setLanguage(getLanguage());
    }

    @Override
    public Path getDirectory() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public int getVersion() {
        return plugin.getConfig().getInt("version", 0);
    }

    @Override
    public String getLanguage() {
        return Input.checkLanguage(plugin.getConfig().getString("language", "en"));
    }

    @Override
    public boolean getContinueOnRestart() {
        return plugin.getConfig().getBoolean("continue-on-restart", false);
    }

    @Override
    public boolean isForceLoadExistingChunks() {
        return plugin.getConfig().getBoolean("force-load-existing-chunks", false);
    }

    @Override
    public boolean isSilent() {
        return plugin.getConfig().getBoolean("silent", false);
    }

    @Override
    public void setSilent(final boolean silent) {
        plugin.getConfig().set("silent", silent);
    }

    @Override
    public int getUpdateInterval() {
        return plugin.getConfig().getInt("update-interval", 1);
    }

    @Override
    public void setUpdateInterval(final int updateInterval) {
        plugin.getConfig().set("update-interval", updateInterval);
    }

    @Override
    public void reload() {
        plugin.reloadConfig();
    }
}
