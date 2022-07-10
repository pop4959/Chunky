package org.popcraft.chunky.command;

import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.Selection;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.popcraft.chunky.util.Translator.translate;

public class TrimCommand extends ChunkyCommand {
    public TrimCommand(final Chunky chunky) {
        super(chunky);
    }

    @Override
    public void execute(final Sender sender, final String[] args) {
        if (args.length > 1) {
            final Optional<World> world = Input.tryWorld(chunky, args[1]);
            if (world.isPresent()) {
                chunky.getSelection().world(world.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        if (args.length > 2) {
            final Optional<String> shape = Input.tryShape(args[2]);
            if (shape.isPresent()) {
                chunky.getSelection().shape(shape.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        if (args.length > 3) {
            final Optional<Double> centerX = Input.tryDoubleSuffixed(args[3]).filter(cx -> !Input.isPastWorldLimit(cx));
            final Optional<Double> centerZ = Input.tryDoubleSuffixed(args.length > 4 ? args[4] : null).filter(cz -> !Input.isPastWorldLimit(cz));
            if (centerX.isPresent() && centerZ.isPresent()) {
                chunky.getSelection().center(centerX.get(), centerZ.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        if (args.length > 5) {
            final Optional<Double> radiusX = Input.tryDoubleSuffixed(args[5]).filter(rx -> rx >= 0 && !Input.isPastWorldLimit(rx));
            if (radiusX.isPresent()) {
                chunky.getSelection().radius(radiusX.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        if (args.length > 6) {
            final Optional<Double> radiusZ = Input.tryDoubleSuffixed(args[6]).filter(rz -> rz >= 0 && !Input.isPastWorldLimit(rz));
            if (radiusZ.isPresent()) {
                chunky.getSelection().radiusZ(radiusZ.get());
            } else {
                sender.sendMessage(TranslationKey.HELP_TRIM);
                return;
            }
        }
        final Selection selection = chunky.getSelection().build();
        final Shape shape = ShapeFactory.getShape(selection);
        final Runnable deletionAction = () -> chunky.getScheduler().runTask(() -> {
            sender.sendMessagePrefixed(TranslationKey.FORMAT_START, selection.world().getName(), translate("shape_" + selection.shape()), Formatting.number(selection.centerX()), Formatting.number(selection.centerZ()), Formatting.radius(selection));
            final Optional<Path> regionPath = selection.world().getRegionDirectory();
            final Optional<Path> poiPath = selection.world().getPOIDirectory();
            final Optional<Path> entitiesPath = selection.world().getEntitiesDirectory();
            final AtomicLong deleted = new AtomicLong();
            final long startTime = System.currentTimeMillis();
            try {
                if (regionPath.isPresent()) {
                    try (final Stream<Path> regionWalker = Files.walk(regionPath.get())) {
                        regionWalker.forEach(region -> deleted.getAndAdd(checkRegion(region, shape)));
                    }
                }
                if (poiPath.isPresent()) {
                    try (final Stream<Path> poiWalker = Files.walk(poiPath.get())) {
                        poiWalker.forEach(region -> checkRegion(region, shape));
                    }
                }
                if (entitiesPath.isPresent()) {
                    try (final Stream<Path> entityWalker = Files.walk(entitiesPath.get())) {
                        entityWalker.forEach(region -> checkRegion(region, shape));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            final long totalTime = System.currentTimeMillis() - startTime;
            sender.sendMessagePrefixed(TranslationKey.TASK_TRIM, deleted.get(), selection.world().getName(), String.format("%.3f", totalTime / 1e3f));
        });
        chunky.setPendingAction(sender, deletionAction);
        sender.sendMessagePrefixed(TranslationKey.FORMAT_TRIM_CONFIRM, selection.world().getName(), translate("shape_" + selection.shape()), Formatting.number(selection.centerX()), Formatting.number(selection.centerZ()), Formatting.radius(selection), "/chunky confirm");
    }

    private int checkRegion(final Path region, final Shape shape) {
        final Optional<ChunkCoordinate> regionCoordinate = tryRegionCoordinate(region);
        if (regionCoordinate.isEmpty()) {
            return 0;
        }
        final int chunkX = regionCoordinate.get().x() << 5;
        final int chunkZ = regionCoordinate.get().z() << 5;
        if (shouldDeleteRegion(shape, chunkX, chunkZ)) {
            return deleteRegion(region);
        } else {
            return trimRegion(region, shape, chunkX, chunkZ);
        }
    }

    private Optional<ChunkCoordinate> tryRegionCoordinate(final Path region) {
        final String fileName = region.getFileName().toString();
        if (!fileName.startsWith("r.")) {
            return Optional.empty();
        }
        final int extension = fileName.indexOf(".mca");
        if (extension < 2) {
            return Optional.empty();
        }
        final String regionCoordinates = fileName.substring(2, extension);
        final int separator = regionCoordinates.indexOf('.');
        final Optional<Integer> regionX = Input.tryInteger(regionCoordinates.substring(0, separator));
        final Optional<Integer> regionZ = Input.tryInteger(regionCoordinates.substring(separator + 1));
        if (regionX.isPresent() && regionZ.isPresent()) {
            return Optional.of(new ChunkCoordinate(regionX.get(), regionZ.get()));
        }
        return Optional.empty();
    }

    private boolean shouldDeleteRegion(final Shape shape, final int chunkX, final int chunkZ) {
        for (int offsetX = 0; offsetX < 32; ++offsetX) {
            for (int offsetZ = 0; offsetZ < 32; ++offsetZ) {
                final int chunkCenterX = ((chunkX + offsetX) << 4) + 8;
                final int chunkCenterZ = ((chunkZ + offsetZ) << 4) + 8;
                if (shape.isBounding(chunkCenterX, chunkCenterZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int deleteRegion(final Path region) {
        try {
            Files.deleteIfExists(region);
            return 1024;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int trimRegion(final Path region, final Shape shape, final int chunkX, final int chunkZ) {
        int deleted = 0;
        try (RandomAccessFile regionFile = new RandomAccessFile(region.toFile(), "rw")) {
            if (regionFile.length() < 4096) {
                return 0;
            }
            for (int offsetX = 0; offsetX < 32; ++offsetX) {
                for (int offsetZ = 0; offsetZ < 32; ++offsetZ) {
                    final int chunkCenterX = ((chunkX + offsetX) << 4) + 8;
                    final int chunkCenterZ = ((chunkZ + offsetZ) << 4) + 8;
                    if (!shape.isBounding(chunkCenterX, chunkCenterZ)) {
                        final int chunkLocation = ((offsetX % 32) + (offsetZ % 32) * 32) * 4;
                        regionFile.seek(chunkLocation);
                        if (regionFile.readInt() != 0) {
                            regionFile.seek(chunkLocation);
                            regionFile.writeInt(0);
                            ++deleted;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deleted;
    }

    @Override
    public List<String> tabSuggestions(final String[] args) {
        if (args.length == 2) {
            final List<String> suggestions = new ArrayList<>();
            chunky.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
            return suggestions;
        } else if (args.length == 3) {
            return ShapeType.ALL;
        }
        return Collections.emptyList();
    }
}
