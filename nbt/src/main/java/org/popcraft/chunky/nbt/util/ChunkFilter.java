package org.popcraft.chunky.nbt.util;

import org.popcraft.chunky.nbt.CompoundTag;
import org.popcraft.chunky.nbt.TagType;

import java.util.Set;
import java.util.function.Predicate;

public final class ChunkFilter {
    private final byte type;
    private final String name;
    private final Set<String> tagNames;
    private final Predicate<CompoundTag> stop;

    private ChunkFilter(final byte type, final String name, final Set<String> tagNames, final Predicate<CompoundTag> stop) {
        this.type = type;
        this.name = name;
        this.tagNames = tagNames;
        this.stop = stop;
    }

    public static ChunkFilter of(final byte type, final String name) {
        return new ChunkFilter(type, name, null, null);
    }

    public static ChunkFilter ofTags(final String... tagNames) {
        return ofTags(null, tagNames);
    }

    public static ChunkFilter ofTags(final Predicate<CompoundTag> stop, final String... tagNames) {
        return new ChunkFilter(TagType.COMPOUND, null, Set.of(tagNames), stop);
    }

    public byte getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean hasTagNames() {
        return tagNames != null;
    }

    public Set<String> getTagNames() {
        return tagNames;
    }

    public Predicate<CompoundTag> getStop() {
        return stop;
    }
}
