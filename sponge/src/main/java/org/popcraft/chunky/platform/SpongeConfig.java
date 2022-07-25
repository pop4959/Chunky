package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkySponge;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Translator;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpongeConfig implements Config {
    private static final String CONFIG_FILE = "main.conf";
    private static final String ROOT_CONFIG_NODE = "config";
    private final ChunkySponge plugin;
    private final HoconConfigurationLoader configLoader;
    private CommentedConfigurationNode rootNode;

    public SpongeConfig(final ChunkySponge plugin) {
        this.plugin = plugin;
        final Path defaultConfigPath = plugin.getConfigPath();
        try {
            Files.createDirectories(defaultConfigPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Path defaultConfigFile = new File(defaultConfigPath.toFile(), CONFIG_FILE).toPath();
        this.configLoader = HoconConfigurationLoader.builder()
                .path(defaultConfigFile)
                .build();
        try {
            this.rootNode = configLoader.load();
        } catch (IOException e) {
            this.rootNode = configLoader.createNode();
            e.printStackTrace();
        }
        final URL defaults = getClass().getClassLoader().getResource(CONFIG_FILE);
        if (defaults != null) {
            final HoconConfigurationLoader defaultConfigLoader = HoconConfigurationLoader.builder()
                    .url(defaults)
                    .build();
            try {
                final CommentedConfigurationNode defaultRootNode = defaultConfigLoader.load();
                rootNode.mergeFrom(defaultRootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            configLoader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Translator.setLanguage(getLanguage());
    }

    @Override
    public Path getDirectory() {
        return plugin.getConfigPath();
    }

    @Override
    public int getVersion() {
        return this.rootNode == null ? 0 : this.rootNode.node(ROOT_CONFIG_NODE, "version").getInt(0);
    }

    @Override
    public String getLanguage() {
        if (this.rootNode == null) {
            return "en";
        }
        return Input.checkLanguage(this.rootNode.node(ROOT_CONFIG_NODE, "language").getString("en"));
    }

    @Override
    public boolean getContinueOnRestart() {
        return this.rootNode != null && this.rootNode.node(ROOT_CONFIG_NODE, "continue-on-restart").getBoolean(false);
    }

    @Override
    public boolean isSilent() {
        return this.rootNode != null && this.rootNode.node(ROOT_CONFIG_NODE, "silent").getBoolean(false);
    }

    @Override
    public void setSilent(final boolean silent) {
        if (this.rootNode != null) {
            try {
                this.rootNode.node(ROOT_CONFIG_NODE, "silent").set(silent);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getUpdateInterval() {
        return this.rootNode == null ? 1 : this.rootNode.node(ROOT_CONFIG_NODE, "update-interval").getInt(1);
    }

    @Override
    public void setUpdateInterval(final int updateInterval) {
        if (this.rootNode != null) {
            try {
                this.rootNode.node(ROOT_CONFIG_NODE, "update-interval").set(updateInterval);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reload() {
        try {
            this.rootNode = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
