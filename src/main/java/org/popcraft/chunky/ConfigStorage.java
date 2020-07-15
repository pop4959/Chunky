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
    private final static String TASKS_KEY = "tasks.";

    public ConfigStorage(Chunky chunky) {
        this.chunky = chunky;
        this.config = chunky.getConfig();
    }

    public Optional<GenTask> loadTask(World world) {
        if (config.getConfigurationSection(TASKS_KEY + world.getName()) == null) {
            return Optional.empty();
        }
        String world_key = TASKS_KEY + world.getName() + ".";
        int radius = config.getInt(world_key + "radius");
        int centerX = config.getInt(world_key + "x-center");
        int centerZ = config.getInt(world_key + "z-center");
        long count = config.getLong(world_key + "count");
        return Optional.of(new GenTask(chunky, world, radius, centerX, centerZ, count));
    }

    public List<GenTask> loadTasks() {
        List<GenTask> genTasks = new ArrayList<>();
        chunky.getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(genTasks::add));
        return genTasks;
    }

    public void saveTask(GenTask genTask) {
        String world_key = TASKS_KEY + genTask.getWorld().getName() + ".";
        config.set(world_key + "radius", genTask.getRadius());
        config.set(world_key + "x-center", genTask.getCenterX());
        config.set(world_key + "z-center", genTask.getCenterZ());
        ChunkCoordinate currentChunk = genTask.getChunkCoordinateIterator().peek();
        config.set(world_key + "x-chunk", currentChunk.x);
        config.set(world_key + "z-chunk", currentChunk.z);
        config.set(world_key + "count", genTask.getCount());
        chunky.saveConfig();
    }

    public void saveTasks() {
        chunky.getGenTasks().values().forEach(this::saveTask);
    }

    public void reset() {
        File file = new File(chunky.getDataFolder(), "config.yml");
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        chunky.saveDefaultConfig();
    }
}
