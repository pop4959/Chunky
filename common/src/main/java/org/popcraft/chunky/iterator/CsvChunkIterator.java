package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class CsvChunkIterator implements ChunkIterator {
    private final Queue<ChunkCoordinate> chunks;
    private final long total;
    private final String name;

    public CsvChunkIterator(final Selection selection, final long count) {
        this(selection);
        for (int i = 0; i < count && hasNext(); ++i) {
            chunks.poll();
        }
    }

    public CsvChunkIterator(final Selection selection) {
        final Path filePath = selection.pattern().getValue()
                .map(value -> selection.chunky().getConfig().getDirectory().resolve(String.format("%s.csv", value)))
                .orElse(null);
        this.chunks = new LinkedList<>();
        final AtomicLong valid = new AtomicLong();
        if (filePath != null) {
            try (final Stream<String> lines = Files.lines(filePath)) {
                lines.forEach(line -> {
                    final String[] split = line.split(",");
                    if (split.length > 1) {
                        final Optional<Integer> x = Input.tryInteger(split[0]);
                        final Optional<Integer> z = Input.tryInteger(split[1]);
                        if (x.isPresent() && z.isPresent()) {
                            chunks.add(new ChunkCoordinate(x.get(), z.get()));
                            valid.incrementAndGet();
                        }
                    }
                });
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        this.total = valid.get();
        this.name = selection.pattern().toString();
    }

    @Override
    public boolean hasNext() {
        return !chunks.isEmpty();
    }

    @Override
    public ChunkCoordinate next() {
        if (chunks.isEmpty()) {
            throw new NoSuchElementException();
        }
        return chunks.poll();
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public String name() {
        return name;
    }
}
