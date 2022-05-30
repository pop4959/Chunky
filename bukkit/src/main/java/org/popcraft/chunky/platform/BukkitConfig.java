package org.popcraft.chunky.platform;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.iterator.PatternType;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Parameter;
import org.popcraft.chunky.util.Translator;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BukkitConfig implements Config {
    private static final String TASKS_KEY = "tasks.";
    private static final List<String> HEADER = Arrays.asList("Chunky Configuration", "https://github.com/pop4959/Chunky/wiki/Configuration");
    private final ChunkyBukkit plugin;

    public BukkitConfig(ChunkyBukkit plugin) {
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
    public synchronized Optional<GenerationTask> loadTask(World world) {
        FileConfiguration config = plugin.getConfig();
        if (config.getConfigurationSection(TASKS_KEY + world.getName()) == null) {
            return Optional.empty();
        }
        String worldKey = TASKS_KEY + world.getName() + ".";
        boolean cancelled = config.getBoolean(worldKey + "cancelled", false);
        double radiusX = config.getDouble(worldKey + "radius", Selection.DEFAULT_RADIUS);
        double radiusZ = config.getDouble(worldKey + "z-radius", radiusX);
        Selection.Builder selection = Selection.builder(plugin.getChunky(), world)
                .centerX(config.getDouble(worldKey + "x-center", Selection.DEFAULT_CENTER_X))
                .centerZ(config.getDouble(worldKey + "z-center", Selection.DEFAULT_CENTER_Z))
                .radiusX(radiusX)
                .radiusZ(radiusZ)
                .pattern(Parameter.of(config.getString(worldKey + "iterator", PatternType.CONCENTRIC)))
                .shape(config.getString(worldKey + "shape", ShapeType.SQUARE));
        long count = config.getLong(worldKey + "count", 0);
        long time = config.getLong(worldKey + "time", 0);
        return Optional.of(new GenerationTask(plugin.getChunky(), selection.build(), count, time, cancelled));
    }

    @Override
    public synchronized List<GenerationTask> loadTasks() {
        List<GenerationTask> generationTasks = new ArrayList<>();
        plugin.getChunky().getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
        return generationTasks;
    }

    @Override
    public synchronized void saveTask(GenerationTask generationTask) {
        FileConfiguration config = plugin.getConfig();
        Selection selection = generationTask.getSelection();
        String worldKey = TASKS_KEY + selection.world().getName() + ".";
        String shape = generationTask.getShape().name();
        config.set(worldKey + "cancelled", generationTask.isCancelled());
        config.set(worldKey + "radius", selection.radiusX());
        if (ShapeType.RECTANGLE.equals(shape) || ShapeType.ELLIPSE.equals(shape)) {
            config.set(worldKey + "z-radius", selection.radiusZ());
        }
        config.set(worldKey + "x-center", selection.centerX());
        config.set(worldKey + "z-center", selection.centerZ());
        config.set(worldKey + "iterator", generationTask.getChunkIterator().name());
        config.set(worldKey + "shape", shape);
        config.set(worldKey + "count", generationTask.getCount());
        config.set(worldKey + "time", generationTask.getTotalTime());
        plugin.saveConfig();
    }

    @Override
    public synchronized void saveTasks() {
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
    public synchronized void cancelTasks() {
        loadTasks().forEach(generationTask -> {
            generationTask.stop(true);
            saveTask(generationTask);
        });
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
    public boolean isSilent() {
        return plugin.getConfig().getBoolean("silent", false);
    }

    @Override
    public void setSilent(boolean silent) {
        plugin.getConfig().set("silent", silent);
    }

    @Override
    public int getUpdateInterval() {
        return plugin.getConfig().getInt("update-interval", 1);
    }

    @Override
    public void setUpdateInterval(int updateInterval) {
        plugin.getConfig().set("update-interval", updateInterval);
    }

    @Override
    public void reload() {
        plugin.reloadConfig();
    }
}
