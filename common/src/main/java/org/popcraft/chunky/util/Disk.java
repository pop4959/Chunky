package org.popcraft.chunky.util;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.World;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class Disk {
    private static final long ESTIMATED_SPACE_PER_CHUNK = 7031L;
    private static final double PERCENT_OVERESTIMATE = 1.05d;

    private Disk() {
    }

    public static long estimatedSpace(final Selection selection) {
        final long diameterChunksX = selection.diameterChunksX();
        final long diameterChunksZ = selection.diameterChunksZ();
        return (long) (PERCENT_OVERESTIMATE * (diameterChunksX * diameterChunksZ * ESTIMATED_SPACE_PER_CHUNK));
    }

    public static long remainingSpace(final World world) {
        final Optional<Path> regionDirectory = world.getRegionDirectory();
        try {
            if (regionDirectory.isPresent()) {
                return regionDirectory.get().toFile().getUsableSpace();
            }
            final Path currentWorkingDirectory = Paths.get("");
            if (Files.exists(currentWorkingDirectory)) {
                return currentWorkingDirectory.toFile().getUsableSpace();
            }
        } catch (UnsupportedOperationException | InvalidPathException | SecurityException ignored) {
        }
        return 0L;
    }
}
