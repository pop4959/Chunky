package org.popcraft.chunky;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigStorage {
    private final Chunky chunky;
    private final FileConfiguration config;
    private static final String TASKS_KEY = "tasks.";

    public ConfigStorage(Chunky chunky) {
        this.chunky = chunky;
        this.config = chunky.getConfig();
    }

    public synchronized Optional<GenerationTask> loadTask(World world) {
        if (config.getConfigurationSection(TASKS_KEY + world.getName()) == null) {
            return Optional.empty();
        }
        String world_key = TASKS_KEY + world.getName() + ".";
        if (config.getBoolean(world_key + "cancelled", false)) {
            return Optional.empty();
        }
        Selection selection = new Selection();
        selection.world = world;
        selection.radiusX = config.getInt(world_key + "radius", 500);
        selection.radiusZ = config.getInt(world_key + "z-radius", selection.radiusX);
        selection.centerX = config.getInt(world_key + "x-center", 0);
        selection.centerZ = config.getInt(world_key + "z-center", 0);
        selection.pattern = config.getString(world_key + "iterator", "loop");
        selection.shape = config.getString(world_key + "shape", "square");
        long count = config.getLong(world_key + "count", 0);
        long time = config.getLong(world_key + "time", 0);
        return Optional.of(new GenerationTask(chunky, selection, count, time));
    }

    public synchronized List<GenerationTask> loadTasks() {
        List<GenerationTask> generationTasks = new ArrayList<>();
        chunky.getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
        return generationTasks;
    }

    public synchronized void saveTask(GenerationTask generationTask) {
        String world_key = TASKS_KEY + generationTask.getWorld().getName() + ".";
        String shape = generationTask.getShape().name();
        config.set(world_key + "cancelled", generationTask.isCancelled());
        config.set(world_key + "radius", generationTask.getRadiusX());
        if ("rectangle".equals(shape) || "oval".equals(shape)) {
            config.set(world_key + "z-radius", generationTask.getRadiusZ());
        }
        config.set(world_key + "x-center", generationTask.getCenterX());
        config.set(world_key + "z-center", generationTask.getCenterZ());
        config.set(world_key + "iterator", generationTask.getChunkIterator().name());
        config.set(world_key + "shape", shape);
        config.set(world_key + "count", generationTask.getCount());
        config.set(world_key + "time", generationTask.getTotalTime());
        chunky.saveConfig();
    }

    public synchronized void saveTasks() {
        chunky.getGenerationTasks().values().forEach(this::saveTask);
    }

    public synchronized void cancelTasks() {
        loadTasks().forEach(generationTask -> {
            generationTask.stop(true);
            saveTask(generationTask);
        });
    }
}
