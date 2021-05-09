package org.popcraft.chunky.platform;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BukkitConfig implements Config {
    private final Chunky chunky;
    private final JavaPlugin plugin;
    private static final String TASKS_KEY = "tasks.";

    public BukkitConfig(Chunky chunky, JavaPlugin plugin) {
        this.chunky = chunky;
        this.plugin = plugin;
        plugin.getConfig().options().copyDefaults(true);
        plugin.getConfig().options().copyHeader(true);
        plugin.saveConfig();
    }

    @Override
    public synchronized Optional<GenerationTask> loadTask(World world) {
        FileConfiguration config = plugin.getConfig();
        if (config.getConfigurationSection(TASKS_KEY + world.getName()) == null) {
            return Optional.empty();
        }
        String world_key = TASKS_KEY + world.getName() + ".";
        if (config.getBoolean(world_key + "cancelled", false)) {
            return Optional.empty();
        }
        double radiusX = config.getDouble(world_key + "radius", 500);
        double radiusZ = config.getDouble(world_key + "z-radius", radiusX);
        Selection.Builder selection = Selection.builder(world)
                .centerX(config.getDouble(world_key + "x-center", 0))
                .centerZ(config.getDouble(world_key + "z-center", 0))
                .radiusX(radiusX)
                .radiusZ(radiusZ)
                .pattern(config.getString(world_key + "iterator", "loop"))
                .shape(config.getString(world_key + "shape", "square"));
        long count = config.getLong(world_key + "count", 0);
        long time = config.getLong(world_key + "time", 0);
        return Optional.of(new GenerationTask(chunky, selection.build(), count, time));
    }

    @Override
    public synchronized List<GenerationTask> loadTasks() {
        List<GenerationTask> generationTasks = new ArrayList<>();
        chunky.getPlatform().getServer().getWorlds().forEach(world -> loadTask(world).ifPresent(generationTasks::add));
        return generationTasks;
    }

    @Override
    public synchronized void saveTask(GenerationTask generationTask) {
        FileConfiguration config = plugin.getConfig();
        Selection selection = generationTask.getSelection();
        String world_key = TASKS_KEY + selection.world().getName() + ".";
        String shape = generationTask.getShape().name();
        config.set(world_key + "cancelled", generationTask.isCancelled());
        config.set(world_key + "radius", selection.radiusX());
        if ("rectangle".equals(shape) || "oval".equals(shape)) {
            config.set(world_key + "z-radius", selection.radiusZ());
        }
        config.set(world_key + "x-center", selection.centerX());
        config.set(world_key + "z-center", selection.centerZ());
        config.set(world_key + "iterator", generationTask.getChunkIterator().name());
        config.set(world_key + "shape", shape);
        config.set(world_key + "count", generationTask.getCount());
        config.set(world_key + "time", generationTask.getTotalTime());
        plugin.saveConfig();
    }

    @Override
    public synchronized void saveTasks() {
        chunky.getGenerationTasks().values().forEach(this::saveTask);
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
    public void reload() {
        plugin.reloadConfig();
    }
}
