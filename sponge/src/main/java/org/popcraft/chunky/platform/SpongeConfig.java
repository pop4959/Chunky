package org.popcraft.chunky.platform;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.popcraft.chunky.ChunkySponge;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.Input;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpongeConfig implements Config {
    private final ChunkySponge plugin;
    private final HoconConfigurationLoader configLoader;
    private CommentedConfigurationNode rootNode;

    public SpongeConfig(ChunkySponge plugin) {
        this.plugin = plugin;
        Path defaultConfigPath = plugin.getConfigPath();
        try {
            Files.createDirectories(defaultConfigPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path defaultConfigFile = new File(defaultConfigPath.toFile(), "main.conf").toPath();
        this.configLoader = HoconConfigurationLoader.builder()
                .setPath(defaultConfigFile)
                .build();
        try {
            this.rootNode = configLoader.load();
        } catch (IOException e) {
            this.rootNode = configLoader.createEmptyNode();
            e.printStackTrace();
        }
        URL defaults = getClass().getClassLoader().getResource("main.conf");
        if (defaults != null) {
            final HoconConfigurationLoader defaultConfigLoader = HoconConfigurationLoader.builder()
                    .setURL(defaults)
                    .build();
            try {
                CommentedConfigurationNode defaultRootNode = defaultConfigLoader.load();
                rootNode.mergeValuesFrom(defaultRootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            configLoader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<GenerationTask> loadTask(World world) {
        if (this.rootNode == null) {
            return Optional.empty();
        }
        ConfigurationNode taskNode = rootNode.getNode("config", "tasks", world.getName());
        if (taskNode.isVirtual()) {
            return Optional.empty();
        }
        if (taskNode.getNode("cancelled").getBoolean(true)) {
            return Optional.empty();
        }
        int radiusX = taskNode.getNode("radius").getInt(500);
        int radiusZ = taskNode.getNode("radiusZ").getInt(radiusX);
        Selection.Builder selection = Selection.builder(world)
                .centerX(taskNode.getNode("centerX").getInt(0))
                .centerZ(taskNode.getNode("centerZ").getInt(0))
                .radiusX(radiusX)
                .radiusZ(radiusZ)
                .pattern(taskNode.getNode("iterator").getString("concentric"))
                .shape(taskNode.getNode("shape").getString("square"));
        long count = taskNode.getNode("count").getInt(0);
        long time = taskNode.getNode("time").getInt(0);
        return Optional.of(new GenerationTask(plugin.getChunky(), selection.build(), count, time));
    }

    @Override
    public List<GenerationTask> loadTasks() {
        List<GenerationTask> generationTasks = new ArrayList<>();
        plugin.getChunky().getPlatform().getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
        return generationTasks;
    }

    @Override
    public void saveTask(GenerationTask generationTask) {
        if (this.rootNode == null) {
            this.rootNode = configLoader.createEmptyNode();
        }
        Selection selection = generationTask.getSelection();
        ConfigurationNode taskNode = rootNode.getNode("config", "tasks", selection.world().getName());
        String shape = generationTask.getShape().name();
        taskNode.getNode("cancelled").setValue(generationTask.isCancelled());
        taskNode.getNode("radius").setValue(selection.radiusX());
        if ("rectangle".equals(shape) || "oval".equals(shape)) {
            taskNode.getNode("radiusZ").setValue(selection.radiusZ());
        }
        taskNode.getNode("centerX").setValue(selection.centerX());
        taskNode.getNode("centerZ").setValue(selection.centerZ());
        taskNode.getNode("iterator").setValue(generationTask.getChunkIterator().name());
        taskNode.getNode("shape").setValue(shape);
        taskNode.getNode("count").setValue(generationTask.getCount());
        taskNode.getNode("time").setValue(generationTask.getTotalTime());
        try {
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
    public void cancelTask(World world) {
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
        return this.rootNode == null ? 0 : this.rootNode.getNode("version").getInt(0);
    }

    @Override
    public String getLanguage() {
        if (this.rootNode == null) {
            return "en";
        }
        return Input.checkLanguage(this.rootNode.getNode("language").getString("en"));
    }

    @Override
    public boolean getContinueOnRestart() {
        return this.rootNode != null && this.rootNode.getNode("continue-on-restart").getBoolean(false);
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
