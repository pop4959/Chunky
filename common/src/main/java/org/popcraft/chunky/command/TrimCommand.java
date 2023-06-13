package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.nbt.CompoundTag;
import org.popcraft.chunky.nbt.LongTag;
import org.popcraft.chunky.nbt.util.RegionFile;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.Shape;
import org.popcraft.chunky.shape.ShapeFactory;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Formatting;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.TranslationKey;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.popcraft.chunky.util.Translator.translate;

public class TrimCommand implements ChunkyCommand {
    private final Chunky chunky;

    public TrimCommand(final Chunky chunky) {
        this.chunky = chunky;
    }

    @Override
    public void execute(final Sender sender, final CommandArguments arguments) {
        if (arguments.size() > 0) {
            final Optional<World> world = arguments.next().flatMap(arg -> Input.tryWorld(chunky, arg));
            if (world.isPresent()) {
                chunky.getSelection().world(world.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        if (arguments.size() > 1) {
            final Optional<String> shape = arguments.next().flatMap(Input::tryShape);
            if (shape.isPresent()) {
                chunky.getSelection().shape(shape.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        if (arguments.size() > 2) {
            final Optional<Double> centerX = arguments.next().flatMap(Input::tryDoubleSuffixed).filter(c -> !Input.isPastWorldLimit(c));
            final Optional<Double> centerZ = arguments.next().flatMap(Input::tryDoubleSuffixed).filter(c -> !Input.isPastWorldLimit(c));
            if (centerX.isPresent() && centerZ.isPresent()) {
                chunky.getSelection().center(centerX.get(), centerZ.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        if (arguments.size() > 4) {
            final Optional<Double> radiusX = arguments.next().flatMap(Input::tryDoubleSuffixed).filter(r -> r >= 0 && !Input.isPastWorldLimit(r));
            if (radiusX.isPresent()) {
                chunky.getSelection().radius(radiusX.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        if (arguments.size() > 5) {
            final Optional<Double> radiusZ = arguments.next().flatMap(Input::tryDoubleSuffixed).filter(r -> r >= 0 && !Input.isPastWorldLimit(r));
            if (radiusZ.isPresent()) {
                chunky.getSelection().radiusZ(radiusZ.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        final boolean inside = arguments.next().map(String::toLowerCase).map("inside"::equals).orElse(false);
        final int inhabitedTime = arguments.next().flatMap(Input::tryIntegerSuffixed).orElse(Integer.MAX_VALUE);
        final boolean inhabitedTimeCheck = inhabitedTime < Integer.MAX_VALUE;
        final Selection selection = chunky.getSelection().build();
        final Shape shape = ShapeFactory.getShape(selection);
        final Runnable deletionAction = () -> chunky.getScheduler().runTask(() -> {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_START, selection.world().getName(), translate("shape_" + selection.shape()), Formatting.number(selection.centerX()), Formatting.number(selection.centerZ()), Formatting.radius(selection));
            final Optional<Path> regionPath = selection.world().getRegionDirectory();
            final AtomicLong deleted = new AtomicLong();
            final long startTime = System.currentTimeMillis();
            try {
                if (regionPath.isPresent()) {
                    try (final Stream<Path> regionWalker = Files.walk(regionPath.get())) {
                        regionWalker.forEach(region -> deleted.getAndAdd(checkRegion(selection.world(), region.getFileName().toString(), shape, inside, inhabitedTimeCheck, inhabitedTime)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            final long totalTime = System.currentTimeMillis() - startTime;
            sender.sendMessagePrefixed(TranslationKey.TASK_TRIM, deleted.get(), selection.world().getName(), String.format("%.3f", totalTime / 1e3f));
        });
        chunky.setPendingAction(sender, deletionAction);
        sender.sendMessagePrefixed(inside ? TranslationKey.FORMAT_TRIM_CONFIRM_INSIDE : TranslationKey.FORMAT_TRIM_CONFIRM, selection.world().getName(), translate("shape_" + selection.shape()), Formatting.number(selection.centerX()), Formatting.number(selection.centerZ()), Formatting.radius(selection), "/chunky confirm");
        if (inhabitedTimeCheck) {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_TRIM_CONFIRM_INHABITED, Formatting.number(inhabitedTime));
        }
    }

    private int checkRegion(final World world, final String regionFileName, final Shape shape, final boolean inside, final boolean inhabitedTimeCheck, final int inhabitedTime) {
        final Optional<ChunkCoordinate> regionCoordinate = tryRegionCoordinate(regionFileName);
        if (regionCoordinate.isEmpty()) {
            return 0;
        }
        final int chunkX = regionCoordinate.get().x() << 5;
        final int chunkZ = regionCoordinate.get().z() << 5;
        if (!inhabitedTimeCheck && shouldDeleteRegion(shape, inside, chunkX, chunkZ)) {
            return deleteRegion(world, regionFileName);
        } else {
            return trimRegion(world, regionFileName, shape, inside, chunkX, chunkZ, inhabitedTimeCheck, inhabitedTime);
        }
    }

    private Optional<ChunkCoordinate> tryRegionCoordinate(final String regionFileName) {
        if (!regionFileName.startsWith("r.")) {
            return Optional.empty();
        }
        final int extension = regionFileName.indexOf(".mca");
        if (extension < 2) {
            return Optional.empty();
        }
        final String regionCoordinates = regionFileName.substring(2, extension);
        final int separator = regionCoordinates.indexOf('.');
        final Optional<Integer> regionX = Input.tryInteger(regionCoordinates.substring(0, separator));
        final Optional<Integer> regionZ = Input.tryInteger(regionCoordinates.substring(separator + 1));
        if (regionX.isPresent() && regionZ.isPresent()) {
            return Optional.of(new ChunkCoordinate(regionX.get(), regionZ.get()));
        }
        return Optional.empty();
    }

    private boolean shouldDeleteRegion(final Shape shape, final boolean inside, final int chunkX, final int chunkZ) {
        for (int offsetX = 0; offsetX < 32; ++offsetX) {
            for (int offsetZ = 0; offsetZ < 32; ++offsetZ) {
                final int chunkCenterX = ((chunkX + offsetX) << 4) + 8;
                final int chunkCenterZ = ((chunkZ + offsetZ) << 4) + 8;
                if (inside != shape.isBounding(chunkCenterX, chunkCenterZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int deleteRegion(final World world, final String regionFileName) {
        try {
            final Path regionPath = world.getRegionDirectory().map(region -> region.resolve(regionFileName)).orElseThrow(IllegalStateException::new);
            Files.deleteIfExists(regionPath);
            final Path poiPath = world.getPOIDirectory().map(region -> region.resolve(regionFileName)).orElse(null);
            if (poiPath != null) {
                Files.deleteIfExists(poiPath);
            }
            final Path entitiesPath = world.getEntitiesDirectory().map(region -> region.resolve(regionFileName)).orElse(null);
            if (entitiesPath != null) {
                Files.deleteIfExists(entitiesPath);
            }
            return 1024;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int trimRegion(final World world, final String regionFileName, final Shape shape, final boolean inside, final int chunkX, final int chunkZ, final boolean inhabitedTimeCheck, final int inhabitedTime) {
        final Path regionPath = world.getRegionDirectory().map(region -> region.resolve(regionFileName)).orElseThrow(IllegalStateException::new);
        final Path poiPath = world.getPOIDirectory().map(region -> region.resolve(regionFileName)).orElse(null);
        final Path entitiesPath = world.getEntitiesDirectory().map(region -> region.resolve(regionFileName)).orElse(null);
        int marked = 0;
        int deleted = 0;
        final RegionFile regionData = inhabitedTimeCheck ? new RegionFile(regionPath.toFile()) : null;
        try (final RandomAccessFile regionFile = new RandomAccessFile(regionPath.toFile(), "rw");
             final RandomAccessFile poiFile = poiPath == null ? null : new RandomAccessFile(poiPath.toFile(), "rw");
             final RandomAccessFile entitiesFile = entitiesPath == null ? null : new RandomAccessFile(entitiesPath.toFile(), "rw")) {
            if (regionFile.length() < 4096) {
                return 0;
            }
            final boolean poiValid = poiFile != null && poiFile.length() >= 4096;
            final boolean entitiesValid = entitiesFile != null && entitiesFile.length() >= 4096;
            for (int offsetX = 0; offsetX < 32; ++offsetX) {
                for (int offsetZ = 0; offsetZ < 32; ++offsetZ) {
                    final int offsetChunkX = chunkX + offsetX;
                    final int offsetChunkZ = chunkZ + offsetZ;
                    final int chunkCenterX = (offsetChunkX << 4) + 8;
                    final int chunkCenterZ = (offsetChunkZ << 4) + 8;
                    final boolean trimChunk;
                    if (inside) {
                        trimChunk = shape.isBounding(chunkCenterX, chunkCenterZ);
                    } else {
                        trimChunk = !shape.isBounding(chunkCenterX, chunkCenterZ);
                    }
                    final boolean trimInhabited = regionData == null || regionData.getChunk(offsetChunkX, offsetChunkZ)
                            .map(chunk -> {
                                final CompoundTag compoundTag = chunk.getData();
                                if (compoundTag == null) {
                                    return true;
                                }
                                final LongTag inhabited = compoundTag.getLong("InhabitedTime").orElse(null);
                                if (inhabited == null) {
                                    return true;
                                }
                                return inhabited.value() <= inhabitedTime;
                            })
                            .orElse(true);
                    if (trimChunk && trimInhabited) {
                        ++marked;
                        final int chunkLocation = ((offsetX % 32) + (offsetZ % 32) * 32) * 4;
                        regionFile.seek(chunkLocation);
                        if (regionFile.readInt() != 0) {
                            regionFile.seek(chunkLocation);
                            regionFile.writeInt(0);
                            ++deleted;
                        }
                        if (poiValid) {
                            poiFile.seek(chunkLocation);
                            if (poiFile.readInt() != 0) {
                                poiFile.seek(chunkLocation);
                                poiFile.writeInt(0);
                            }
                        }
                        if (entitiesValid) {
                            entitiesFile.seek(chunkLocation);
                            if (entitiesFile.readInt() != 0) {
                                entitiesFile.seek(chunkLocation);
                                entitiesFile.writeInt(0);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inhabitedTimeCheck && marked == 1024) {
            deleteRegion(world, regionFileName);
        }
        return deleted;
    }

    @Override
    public List<String> suggestions(final CommandArguments arguments) {
        if (arguments.size() == 1) {
            final List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        } else if (arguments.size() == 2) {
            return ShapeType.all();
        }
        return List.of();
    }
}
