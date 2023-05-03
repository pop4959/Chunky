package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompoundTag extends Tag {
    private Map<String, Tag> value = new HashMap<>();

    protected CompoundTag(final String name) {
        super(name);
    }

    public CompoundTag(final String name, final Map<String, Tag> value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.value = new HashMap<>();
        Tag tag;
        while (TagType.END != (tag = Tag.load(input)).type()) {
            this.value.put(tag.name(), tag);
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        for (final Tag tag : value.values()) {
            Tag.save(output, tag);
        }
        output.writeByte(TagType.END);
    }

    @Override
    public byte type() {
        return TagType.COMPOUND;
    }

    @Override
    public String typeName() {
        return "TAG_Compound";
    }

    @Override
    public String print(final int level) {
        final int size = value.size();
        final String entry = size == 1 ? "entry" : "entries";
        final String indent = " ".repeat(level * Tag.INDENT);
        final StringBuilder compoundBuilder = new StringBuilder("%s%s('%s'): %d %s".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, size, entry));
        compoundBuilder.append('\n').append(indent).append("{\n");
        for (final Tag tag : value.values()) {
            compoundBuilder.append(tag.print(level + 1)).append('\n');
        }
        compoundBuilder.append(indent).append('}');
        return compoundBuilder.toString();
    }

    public Optional<Tag> get(final String name) {
        return Optional.ofNullable(value.get(name));
    }

    public Optional<ByteArrayTag> getByteArray(final String name) {
        return get(name).filter(ByteArrayTag.class::isInstance).flatMap(tag -> Optional.of((ByteArrayTag) tag));
    }

    public Optional<ByteTag> getByte(final String name) {
        return get(name).filter(ByteTag.class::isInstance).flatMap(tag -> Optional.of((ByteTag) tag));
    }

    public Optional<CompoundTag> getCompound(final String name) {
        return get(name).filter(CompoundTag.class::isInstance).flatMap(tag -> Optional.of((CompoundTag) tag));
    }

    public Optional<DoubleTag> getDouble(final String name) {
        return get(name).filter(DoubleTag.class::isInstance).flatMap(tag -> Optional.of((DoubleTag) tag));
    }

    public Optional<FloatTag> getFloat(final String name) {
        return get(name).filter(FloatTag.class::isInstance).flatMap(tag -> Optional.of((FloatTag) tag));
    }

    public Optional<IntArrayTag> getIntArray(final String name) {
        return get(name).filter(IntArrayTag.class::isInstance).flatMap(tag -> Optional.of((IntArrayTag) tag));
    }

    public Optional<IntTag> getInt(final String name) {
        return get(name).filter(IntTag.class::isInstance).flatMap(tag -> Optional.of((IntTag) tag));
    }

    public Optional<ListTag> getList(final String name) {
        return get(name).filter(ListTag.class::isInstance).flatMap(tag -> Optional.of((ListTag) tag));
    }

    public Optional<LongArrayTag> getLongArray(final String name) {
        return get(name).filter(LongArrayTag.class::isInstance).flatMap(tag -> Optional.of((LongArrayTag) tag));
    }

    public Optional<LongTag> getLong(final String name) {
        return get(name).filter(LongTag.class::isInstance).flatMap(tag -> Optional.of((LongTag) tag));
    }

    public Optional<ShortTag> getShort(final String name) {
        return get(name).filter(ShortTag.class::isInstance).flatMap(tag -> Optional.of((ShortTag) tag));
    }

    public Optional<StringTag> getString(final String name) {
        return get(name).filter(StringTag.class::isInstance).flatMap(tag -> Optional.of((StringTag) tag));
    }

    public void put(final Tag tag) {
        value.put(tag.name(), tag);
    }

    public void remove(final String name) {
        value.remove(name);
    }
}
