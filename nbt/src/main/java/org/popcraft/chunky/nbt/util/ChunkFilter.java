package org.popcraft.chunky.nbt.util;

import org.popcraft.chunky.nbt.TagType;

import java.util.Set;

public final class ChunkFilter {
    private final byte type;
    private final String name;
    private final Set<String> tagNames;

    private ChunkFilter(final byte type, final String name, final Set<String> tagNames) {
        this.type = type;
        this.name = name;
        this.tagNames = tagNames;
    }

    public static ChunkFilter of(final byte type, final String name) {
        return new ChunkFilter(type, name, null);
    }

    public static ChunkFilter ofTags(final String... tagNames) {
        return new ChunkFilter(TagType.COMPOUND, null, Set.of(tagNames));
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
}
