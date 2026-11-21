package org.popcraft.chunky.nbt.util;

import org.popcraft.chunky.nbt.CompoundTag;
import org.popcraft.chunky.nbt.TagType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class ChunkFilter {
    private final byte type;
    private final String name;
    private final Set<String> tagNames;
    private final Map<String, String> aliases;
    private final Predicate<CompoundTag> stop;

    private ChunkFilter(final byte type, final String name, final Set<String> tagNames, final Map<String, String> aliases, final Predicate<CompoundTag> stop) {
        this.type = type;
        this.name = name;
        this.tagNames = tagNames;
        this.aliases = aliases;
        this.stop = stop;
    }

    public static ChunkFilter of(final byte type, final String name) {
        return new ChunkFilter(type, name, null, Map.of(), null);
    }

    public static ChunkFilter ofTags(final String... tagNames) {
        return ofTags(null, tagNames);
    }

    public static ChunkFilter ofTags(final Predicate<CompoundTag> stop, final String... tagNames) {
        return new ChunkFilter(TagType.COMPOUND, null, Set.of(tagNames), Map.of(), stop);
    }

    public ChunkFilter withAlias(final String tagName, final String alias) {
        if (!tagName.equals(name) && (tagNames == null || !tagNames.contains(tagName))) {
            throw new IllegalArgumentException("Unknown tag %s".formatted(tagName));
        }
        final Map<String, String> aliases = new HashMap<>(this.aliases);
        aliases.put(alias, tagName);
        return new ChunkFilter(type, name, tagNames, Map.copyOf(aliases), stop);
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

    public Map<String, String> getAliases() {
        return aliases;
    }

    public Predicate<CompoundTag> getStop() {
        return stop;
    }
}
