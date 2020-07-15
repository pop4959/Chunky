package org.popcraft.chunky;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Config {
    private final Chunky chunky;
    private final FileConfiguration config;
    private final static String TASKS_KEY = "tasks.";

    public Config(Chunky chunky) {
        this.chunky = chunky;
        this.config = chunky.getConfig();
    }

    public Optional<GenTask> loadTask(World world) {
        if (config.getConfigurationSection(TASKS_KEY + world.getName()) == null) {
            return Optional.empty();
        }
        String world_key = TASKS_KEY + world.getName() + ".";
        int radius = config.getInt(world_key + "radius");
        return Optional.of(new GenTask(chunky, world, radius));
    }

    public List<GenTask> loadTasks() {
        List<GenTask> genTasks = new ArrayList<>();
        chunky.getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(genTasks::add));
        return genTasks;
    }

    public void saveTask(World world) {
        GenTask genTask = chunky.getGenTasks().get(world);
        if (genTask == null) {
            return;
        }
        String world_key = TASKS_KEY + world.getName() + ".";
        config.set(world_key + "radius", genTask.getRadius());
        ChunkCoordinate currentChunk = genTask.getChunkCoordinateIterator().peek();
        config.set(world_key + "x", currentChunk.x);
        config.set(world_key + "z", currentChunk.z);
        chunky.saveDefaultConfig();
    }

    public void saveTasks() {
        chunky.getServer().getWorlds().forEach(this::saveTask);
    }
}
