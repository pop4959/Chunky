package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class Tag {
    protected static final int INDENT = 2;
    protected final String name;

    protected Tag(final String name) {
        this.name = name;
    }

    public static Tag load(final DataInput input) throws IOException {
        final byte type = input.readByte();
        if (TagType.END == type) {
            return new EndTag();
        }
        final String name = input.readUTF();
        final Tag tag = create(type, name);
        tag.read(input);
        return tag;
    }

    public static void save(final DataOutput output, final Tag tag) throws IOException {
        final byte type = tag.type();
        output.writeByte(type);
        if (TagType.END == type) {
            return;
        }
        output.writeUTF(tag.name());
        tag.write(output);
    }

    public static Tag create(final byte type, final String name) {
        return switch (type) {
            case TagType.END -> new EndTag();
            case TagType.BYTE -> new ByteTag(name);
            case TagType.SHORT -> new ShortTag(name);
            case TagType.INT -> new IntTag(name);
            case TagType.LONG -> new LongTag(name);
            case TagType.FLOAT -> new FloatTag(name);
            case TagType.DOUBLE -> new DoubleTag(name);
            case TagType.BYTE_ARRAY -> new ByteArrayTag(name);
            case TagType.STRING -> new StringTag(name);
            case TagType.LIST -> new ListTag(name);
            case TagType.COMPOUND -> new CompoundTag(name);
            case TagType.INT_ARRAY -> new IntArrayTag(name);
            case TagType.LONG_ARRAY -> new LongArrayTag(name);
            default -> throw new IllegalArgumentException("Invalid tag type %d".formatted(type));
        };
    }

    public String name() {
        return name;
    }

    abstract void read(final DataInput input) throws IOException;

    abstract void write(final DataOutput output) throws IOException;

    abstract byte type();

    abstract String typeName();

    abstract String print(final int level);

    @Override
    public String toString() {
        return print(0);
    }
}
