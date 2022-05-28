package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;
import org.popcraft.chunky.util.ChunkCoordinate;
import org.popcraft.chunky.util.Input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

public class CsvChunkIterator implements ChunkIterator {
    private final Queue<ChunkCoordinate> chunks;
    private final long total;
    private final String name;

    public CsvChunkIterator(Selection selection, long count) {
        this(selection);
        for (int i = 0; i < count && hasNext(); ++i) {
            chunks.poll();
        }
    }

    public CsvChunkIterator(Selection selection) {
        this.chunks = selection.pattern().getValue()
                .map(value -> selection.chunky().getConfig().getDirectory().resolve(String.format("%s.csv", value)))
                .map(this::chunks)
                .orElse(new LinkedList<>());
        this.total = chunks.size();
        this.name = selection.pattern().toString();
    }

    private Queue<ChunkCoordinate> chunks(final Path path) {
        try (final Stream<String> lines = Files.lines(path)) {
            final Queue<ChunkCoordinate> queue = new LinkedList<>();
            lines.forEach(line -> {
                final String[] split = line.split(",");
                if (split.length > 1) {
                    final Optional<Integer> x = Input.tryInteger(split[0]);
                    final Optional<Integer> z = Input.tryInteger(split[1]);
                    if (x.isPresent() && z.isPresent()) {
                        queue.add(new ChunkCoordinate(x.get(), z.get()));
                    }
                }
            });
            return queue;
        } catch (final IOException ignored) {
            return new LinkedList<>();
        }
    }

    @Override
    public boolean hasNext() {
        return !chunks.isEmpty();
    }

    @Override
    public ChunkCoordinate next() {
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
