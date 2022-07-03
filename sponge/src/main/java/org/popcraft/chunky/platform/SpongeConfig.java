package org.popcraft.chunky.platform;

import org.popcraft.chunky.ChunkySponge;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.iterator.PatternType;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Parameter;
import org.popcraft.chunky.util.Translator;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Optional<GenerationTask> loadTask(final World world) {
        if (this.rootNode == null) {
            return Optional.empty();
        }
        final ConfigurationNode taskNode = rootNode.node("tasks", world.getName());
        if (taskNode.virtual()) {
            return Optional.empty();
        }
        final boolean cancelled = taskNode.node("cancelled").getBoolean(true);
        final double radiusX = taskNode.node("radius").getDouble(Selection.DEFAULT_RADIUS);
        final double radiusZ = taskNode.node("radiusZ").getDouble(radiusX);
        final Selection.Builder selection = Selection.builder(plugin.getChunky(), world)
                .centerX(taskNode.node("centerX").getDouble(Selection.DEFAULT_CENTER_X))
                .centerZ(taskNode.node("centerZ").getDouble(Selection.DEFAULT_CENTER_Z))
                .radiusX(radiusX)
                .radiusZ(radiusZ)
                .pattern(Parameter.of(taskNode.node("iterator").getString(PatternType.CONCENTRIC)))
                .shape(taskNode.node("shape").getString(ShapeType.SQUARE));
        final long count = taskNode.node("count").getInt(0);
        final long time = taskNode.node("time").getInt(0);
        return Optional.of(new GenerationTask(plugin.getChunky(), selection.build(), count, time, cancelled));
    }

    @Override
    public List<GenerationTask> loadTasks() {
        final List<GenerationTask> generationTasks = new ArrayList<>();
        plugin.getChunky().getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
        return generationTasks;
    }

    @Override
    public void saveTask(final GenerationTask generationTask) {
        if (this.rootNode == null) {
            this.rootNode = configLoader.createNode();
        }
        final Selection selection = generationTask.getSelection();
        final ConfigurationNode taskNode = rootNode.node("tasks", selection.world().getName());
        final String shape = generationTask.getShape().name();
        try {
            taskNode.node("cancelled").set(generationTask.isCancelled());
            taskNode.node("radius").set(selection.radiusX());
            if (ShapeType.RECTANGLE.equals(shape) || ShapeType.ELLIPSE.equals(shape)) {
                taskNode.node("radiusZ").set(selection.radiusZ());
            }
            taskNode.node("centerX").set(selection.centerX());
            taskNode.node("centerZ").set(selection.centerZ());
            taskNode.node("iterator").set(generationTask.getChunkIterator().name());
            taskNode.node("shape").set(shape);
            taskNode.node("count").set(generationTask.getCount());
            taskNode.node("time").set(generationTask.getTotalTime());
            configLoader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveTasks() {
        plugin.getChunky().getGenerationTasks().values().forEach(this::saveTask);
    }

    @Override
    public void cancelTask(final World world) {
        loadTask(world).ifPresent(generationTask -> {
            generationTask.stop(true);
            saveTask(generationTask);
        });
    }

    @Override
    public void cancelTasks() {
        loadTasks().forEach(generationTask -> {
            generationTask.stop(true);
            saveTask(generationTask);
        });
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
