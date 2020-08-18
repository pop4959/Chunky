package org.popcraft.chunky;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
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

    public Optional<GenerationTask> loadTask(World world) {
        if (config.getConfigurationSection(TASKS_KEY + world.getName()) == null) {
            return Optional.empty();
        }
        String world_key = TASKS_KEY + world.getName() + ".";
        if (config.getBoolean(world_key + "cancelled", false)) {
            return Optional.empty();
        }
        int radius = config.getInt(world_key + "radius", 500);
        int centerX = config.getInt(world_key + "x-center", 0);
        int centerZ = config.getInt(world_key + "z-center", 0);
        long count = config.getLong(world_key + "count", 0);
        String iteratorType = config.getString(world_key + "iterator", "loop");
        long time = config.getLong(world_key + "time", 0);
        //noinspection ConstantConditions
        return Optional.of(new GenerationTask(chunky, world, radius, centerX, centerZ, count, iteratorType, time));
    }

    public List<GenerationTask> loadTasks() {
        List<GenerationTask> generationTasks = new ArrayList<>();
        chunky.getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
        return generationTasks;
    }

    public void saveTask(GenerationTask generationTask) {
        String world_key = TASKS_KEY + generationTask.getWorld().getName() + ".";
        config.set(world_key + "cancelled", generationTask.isCancelled());
        config.set(world_key + "radius", generationTask.getRadius());
        config.set(world_key + "x-center", generationTask.getCenterX());
        config.set(world_key + "z-center", generationTask.getCenterZ());
        config.set(world_key + "count", generationTask.getCount());
        config.set(world_key + "iterator", generationTask.getChunkIterator().name());
        config.set(world_key + "time", generationTask.getTotalTime());
        chunky.saveConfig();
    }

    public void saveTasks() {
        chunky.getGenerationTasks().values().forEach(this::saveTask);
    }

    public void cancelTasks() {
        loadTasks().forEach(generationTask -> {
            generationTask.stop(true);
            saveTask(generationTask);
        });
    }

    public void reset() {
        File file = new File(chunky.getDataFolder(), "config.yml");
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        chunky.saveDefaultConfig();
    }
}
